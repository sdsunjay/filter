/*
 * com.spoons.control
 *
 * Copyright (c) 2012-2019 Eriq Augustine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.spoons.control;

import java.io.FileWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * The Logger is a static class that takes care of logging.
 * This class is actually a singleton, but no references will be made available.
 * However, you should make sure to call Logger.init() before you try any logging.
 * If you call init() multiple times, the Logger will check to make sure logging settings
 *  are the same. If they are different, new logs will be opened.
 *  If the Logger has not been init()'ed, it will try and init itself when a logging event
 *  occurs. This is moredangerious because the time that it picks up the logs to use is more
 *  unstable.
 * Just call the static methods.
 *
 * There are five different levels of logging.
 *  INFO  - logging with no level is an alias to logging with INFO.
 *  WARN
 *  DEBUG - Takes an optional Throwable.
 *        - Does not log to a database.
 *  ERROR - Takes an optional Throwable.
 *  FATAL - Takes an optional Throwable.
 *          FATALs will always generate emails and will restart/terminate
 *          the current running instance of SPOONS.
 *
 * If there is an error in one level of logging, the error cascades down to more
 *  sever levels.
 *  (INFO, WARN, DEBUG) -> ERROR -> FATAL
 *
 * Besides logging to files, the Logger also logs to a common database.
 * @TODO(eriq): Log to database.
 * @TODO(eriq): Logging should be asynchronious.
 *  Different levels should get different queues of logging tasks.
 *  Or a single unified priority queue.
 * @TODO(eriq): Handle Fatal shutdown/restart. Tell control to?
 *
 * @author Eriq Augustine &lt;<a href="mailto:eriq.public@gmail.com">eriq.public@gmail.com</a>&gt;
 */
public class Logger {
   /**
    * Log for INFO.
    */
   private static String infoLog = null;

   /**
    * Log for WARN.
    */
   private static String warnLog = null;

   /**
    * Log for DEBUG.
    */
   private static String debugLog = null;

   /**
    * Log for ERROR.
    */
   private static String errorLog = null;

   /**
    * Log for FATAL.
    */
   private static String fatalLog = null;

   /**
    * The name of the current host.
    */
   private static String hostname = null;

   private static boolean inited = false;

   /**
    * This thread is responsible for taking queued up logging tasks
    * and executing them asyncroniously so that logging doesn't block.
    */
   private static LoggingThread loggingThread;

   /**
    * The different logging levels.
    */
   private enum LogLevel {
      INFO,
      DEBUG,
      WARN,
      ERROR,
      FATAL
   }


   /**
    * Must be priavte to enforce singleton behavior.
    */
   private Logger() {
      throw new UnsupportedOperationException();
   }

   /**
    * Init the Logger.
    * This will read file paths from Props.
    * If it can't get the files, it will just email, print, and die.
    */
   public static void init() {
     /* infoLog = Props.getString("INFO_LOG", "log/spoons.log");
      warnLog = Props.getString("WARN_LOG", "log/spoons_warn.log");
      debugLog = Props.getString("DEBUG_LOG", "log/spoons_debug.log");
      errorLog = Props.getString("ERROR_LOG", "log/spoons_error.log");
      fatalLog = Props.getString("FATAL_LOG", "log/spoons_fatal.log");
      hostname = Props.getString("HOST_NAME", getHostname());
      */
      inited = true;

      if (loggingThread == null) {
         loggingThread = new LoggingThread();
         loggingThread.start();
      }
   }

   /**
    * Tear down the Logger.
    * This will kill the logging thread.
    */
   public static void tearDown() {
      flush();
      loggingThread.setDie(true);
      loggingThread.interrupt();

      try {
         loggingThread.join();
      } catch (Exception ex) {
      }
   }

   /**
    * Flush all the logs.
    * This should be unnecessary.
    */
   public static void flush() {
      loggingThread.flush();
   }

   /**
    * Log the given message to the appropriate general log.
    * Use this for general information.
    *
    * If there is an error writting to the normal log,
    *  an error will be logged to the error log.
    *
    * @param message The message to log.
    */
   public static void logInfo(String message) {
      try {
         log(message, null, LogLevel.INFO);
      } catch (Exception e) {
         logError(message, e);
      }
   }

   /**
    * Alias for logInfo().
    */
   public static void log(String message) {
      logInfo(message);
   }

   /**
    * Log the given message to the appropriate error log.
    * Use this whenever an error occurs.
    * For serious errors, use logFatal(String).
    *
    * If there is an error writting to the error log,
    *  an error will be logged to the fatal log.
    *
    * @param message The message to log.
    */
   public static void logError(String message) {
      try {
         log(message, null, LogLevel.ERROR);
      } catch (Exception e) {
         logFatal(message);
      }
   }

   /**
    * Log the given message to the appropriate error log.
    * Use this whenever an error occurs.
    * For serious errors, use logFatal(String).
    *
    * If there is an error writting to the error log,
    *  an error will be logged to the fatal log.
    *
    * @param message The message to log.
    * @param ex Log the exception also.
    */
   public static void logError(String message, Throwable ex) {
      try {
         //Write the exception's message and stack trace to the log message.
         message += "\n";
         message += (ex.toString() + "\n");
         message += (ex.getMessage() + "\n");
         for (StackTraceElement frame : ex.getStackTrace()) {
            message += (frame.toString() + "\n");
         }

         log(message, null, LogLevel.ERROR);
      } catch (Exception e) {
         logFatal(message);
      }
   }

   /**
    * Log the given message to the appropriate alert log.
    * Use this for alerts.
    *
    * If there is an error writting to the alert log,
    *  an error will be logged to the error log.
    *
    * @param message The alert to log.
    */
   public static void logWarn(String message) {
      try {
         log(message, null, LogLevel.WARN);
      } catch (Exception e) {
         logError(message, e);
      }
   }

   /**
    * Log the given message to the appropriate debug log.
    * Use this for debug information.
    *
    * If there is an error writting to the debug log,
    *  an error will be logged to the error log.
    *
    * @param message The message to log.
    */
   public static void logDebug(String message) {
      try {
         log(message, null, LogLevel.DEBUG);
      } catch (Exception e) {
         logError(message, e);
      }
   }

   /**
    * Log the given message to the appropriate debug log.
    * Use this for debug information that has an Exception associated with it.
    *
    * If there is an error writting to the debug log,
    *  an error will be logged to the error log.
    *
    * @param message The message to log.
    * @param ex Log the exception also.
    */
   public static void logDebug(String message, Throwable ex) {
      try {
         //Write the exception's message and stack trace to the log message.
         message += "\n";
         message += (ex.toString() + "\n");
         message += (ex.getMessage() + "\n");
         for (StackTraceElement frame : ex.getStackTrace()) {
            message += (frame.toString() + "\n");
         }

         log(message, null, LogLevel.DEBUG);
      } catch (Exception e) {
         logFatal(message, e);
      }
   }

   /**
    * Log the given message to the appropriate fatal log.
    * Use this for SERIOUS errors.
    * For general errors, use logError(String).
    * Also send out an email with the error.
    *
    * If there is an error writting to the fatal log,
    *  an error will be printed to standard error.
    *
    * @param message The message to log.
    */
   public static void logFatal(String message) {
      // Log to the console first
      System.err.println(hostname + "\n" + message);

      try {
         log(hostname + "\n" + message, null, LogLevel.FATAL);
      } catch (Exception e) {
         System.err.println(message);
         //Control.die();
      }

      flush();
      //Control.die();
   }

   /**
    * Log the given message to the appropriate fatal log.
    * Use this for SERIOUS errors.
    * For general errors, use logError(String).
    * Also send out an email with the error.
    *
    * If there is an error writting to the fatal log,
    *  an error will be printed to standard error.
    *
    * @param message The message to log.
    * @param ex Log the exception also.
    */
   public static void logFatal(String message, Throwable ex) {
      try {
         //Write the exception's message and stack trace to the log message.
         message += "\n";
         message += (ex.toString() + "\n");
         message += (ex.getMessage() + "\n");
         for (StackTraceElement frame : ex.getStackTrace()) {
            message += (frame.toString() + "\n");
         }

         System.err.println(hostname + "\n" + message);
         log(hostname + "\n" + message, null, LogLevel.FATAL);
      } catch (Exception e) {
         System.err.println(message);
         //Control.die();
      }

      flush();
      //Control.die();
   }

   /**
    * Schedule a log.
    *
    * @param message The message to log.
    * @param caller Whoever called into the Logger.
    * @param level The level of logging that was used.
    */
   private static void log(String message, StackTraceElement caller, LogLevel level)  {
      if (!inited) {
         init();
      }

      if (caller == null) {
         caller = Thread.currentThread().getStackTrace()[3];
      }

      String fullMessage = "\n----- " + (new Date()).toString() + " -----\n";
      fullMessage += "**   " + caller.getClassName() + "." +
       caller.getMethodName() + " : " + caller.getLineNumber() + "   **\n";
      fullMessage += message + "\n";
      fullMessage += "----------------------------------------\n";

      queueTask(level, (new Date()).getTime(), fullMessage);
   }

   /**
    * Get the hostname for this machine.
    * If there is an error, 'UNKNOWN' will be returned.
    *
    * @return The hostname of the local machine, 'UNKNOWN' on error.
    */
   private static String getHostname() {
      try {
         return java.net.InetAddress.getLocalHost().getHostName();
      } catch (java.net.UnknownHostException unknownEx) {
         return "UNKNOWN";
      }
   }

   /**
    * Quick wrapper for logging information.
    */
   private static class LoggingTask {
      public LogLevel level;
      public long date;
      public String message;

      public LoggingTask(LogLevel level, long date, String message) {
         this.level = level;
         this.date = date;
         this.message = message;
      }
   }

   /**
    * I don't pass a LoggingTask because I am lazy and don't want to make
    *  a deep constructor.
    */
   private static synchronized void queueTask(LogLevel level, long date, String message) {
      LoggingThread.queue.add(new LoggingTask(level, date, message));
      loggingThread.interrupt();
   }

   /**
    * A thread class to handle all asynchrounious logging tasks.
    */
   private static class LoggingThread extends Thread {
      /**
       * The queue of logging tasks.
       */
      public static Queue<LoggingTask> queue = new LinkedList<LoggingTask>();

      private boolean die;

      public LoggingThread() {
         super();
      }

      public void setDie(boolean toDie) {
         die = toDie;
      }

      /**
       * Flush the queue.
       */
      public void flush() {
         while (!queue.isEmpty()) {
            try {
               boolean email = false;
               String logLevel = null;
               boolean fatal = false;
               boolean database = true;

               LoggingTask task = queue.remove();
               FileWriter writer = null;

               switch (task.level) {
                  case INFO:
                     writer = new FileWriter(infoLog, true);
                     logLevel = "info";
                     break;
                  case DEBUG:
                     writer = new FileWriter(debugLog, true);
                     logLevel = "debug";
                     database = false;
                     break;
                  case WARN:
                     writer = new FileWriter(warnLog, true);
                     logLevel = "warn";
                     break;
                  case ERROR:
                     writer = new FileWriter(errorLog, true);
                     logLevel = "error";
                     break;
                  case FATAL:
                     writer = new FileWriter(fatalLog, true);
                     logLevel = "fatal";
                     email = true;
                     fatal = true;
                     break;
                  default:
                     writer = new FileWriter(fatalLog, true);
                     task.message += "\nUnknown case.\n";
                     logLevel = "fatal";
                     email = true;
                     fatal = true;
                     break;
               }

               writer.append(task.message);
               writer.close();

               if (database) {
                  System.err.println("Database logging has been removed");
               }

               if (email) {
                  System.err.println("Email logging has been removed");
               }

               if (fatal) {
                  //Control.die();
               }
            } catch (Exception ex) {
               System.err.println("Error logging!");
               ex.printStackTrace(System.err);
            }
         }
      }

      /**
       * @inheritDoc
       */
      public void run() {
         while (!die) {
            try {
               // Sleep for 10 seconds.
               sleep(10000);
            } catch (InterruptedException ex) {
               // It is not only okay to be interrupted, it is expected.
               // The LoggingThread is to be interruped whenever there are logs to log.
            }

            flush();
         }
      }
   }
}
