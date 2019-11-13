/*
 * com.spoons.filter
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

package com.spoons.filter;

import com.spoons.control.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class to miscilanious filtering activiting like stripping links.
 */
public class MiscFilters {
   /**
    * Construct a new MiscFilter.
    * Private to promote static behavior.
    */
   private MiscFilters() {
   }

   /**
    * Turn a string into a String[].
    * Stemming will occur, links will be removed, and all non-alpha characters will
    *  be taken out.
    * There will be no extra space around the words.
    *
    * @param text The String to split up. This will be modified.
    *
    * @return The list of words in text.
    */
   public static List<String> fullSplitString(String text) {
      String[] words = new String[0];
      Stemmer stemmer = new Stemmer();

      text = MiscFilters.removeLinks(text);

      text = text.replaceAll("[^a-zA-Z\\s]", " ").trim();
      text = text.replaceAll("\\s\\s+", " ");
      text = text.toLowerCase();
      words = text.split("\\s");
      words = StopWordUtils.removeStopWords(words, 0);

      List<String> rtn = new ArrayList<String>();

      for (int ndx = 0; ndx < rtn.size(); ndx++) {
         rtn.add(stemmer.stem(words[ndx]));
      }

      return rtn;
   }

   /**
    * Turn a string into a String[].
    * Stemming will NOT occur, links will be removed, and all non-alpha characters will
    *  be taken out.
    * There will be no extra space around the words.
    *
    * @param text The String to split up. This will be modified.
    *
    * @return The list of words in text.
    */
   public static List<String> fullSplitStringNoStem(String text) {
      String[] words = new String[0];

      text = MiscFilters.removeLinks(text);

      text = text.replaceAll("[^a-zA-Z\\s]", " ").trim();
      text = text.replaceAll("\\s\\s+", " ");
      text = text.toLowerCase();
      words = text.split("\\s");
      words = StopWordUtils.removeStopWords(words, 0);

      List<String> rtn = new ArrayList<String>();
      for (String word : words) {
         rtn.add(word);
      }

      return rtn;
   }

   /**
    * Split a string into an array of Strings.
    * No Stemming will occur, but all non-alpha characters will
    *  be taken out.
    * There will be no extra space around the words.
    *
    * @param text The String to split up. This will be modified.
    *
    * @return The list of words in text.
    */
   public static String[] splitString(String text) {
      String[] words = new String[0];

      text = text.replaceAll("[^a-zA-Z\\s']", " ").trim();
      text = text.replaceAll("[']", "");
      text = text.replaceAll("\\s\\s+", " ");
      text = text.toLowerCase();
      words = text.split("\\s");

      return words;
   }

   /**
    * Remove hyperlinks from the text and return a clean string.
    *
    * @param input The String to remove hyperlinks from.
    *
    * @return A String without hyperlinks.
    */
   public static String removeLinks(String input) {
      //Note that bitly links dont have an extension.
      //Links must start with a (http(s) or www) or end with an extension.
      String linkRegex = "((\\S*\\.)?www\\.\\S+)|(https?://\\S+\\.\\S+(\\.\\S+)?)|" +
                         "([^\\s\\.]+\\.((com)|(edu)|(org)|(net)|(gov)))(/\\S+)*/?";

      return input.replaceAll(linkRegex, "");
   }

   /**
    * Remove hyperlinks from the text and return a clean string.
    * Note that running this with replacement = "" (empty string) will result
    *  in the same behavior as removeLinks.
    *
    * As per "Cailin's Convention", any bangs ('!') in the replacement string
    *  will be replaced with the matching link.
    *  Ex: (the is a stopword) input = "the dog runs", replacement = "<!>"
    *  output = "<the> dog runs"
    *
    * @param input The String to remove hyperlinks from.
    * @param replacement The string to replace the stopword with.
    *
    * @return A String without hyperlinks.
    *
    * @TODO(eriq): The regex that this method uses has some subtle errors.
    */
   public static String replaceLinks(String input, String replacement) {
      //Note that bitly links dont have an extension.
      //Links must start with a (http(s) or www) or end with an extension.
      String linkRegex = "((\\S*\\.)?www\\.\\S+)|(https?://\\S+\\.\\S+(\\.\\S+)?)|" +
                         "([^\\s\\.]+\\.((com)|(edu)|(org)|(net)|(gov)))(/\\S+)*/?";

      //Escape '$' in the replacemnet string
      replacement = replacement.replaceAll("\\$", "\\\\\\$");
      replacement = replacement.replaceAll("!", "\\$0");

      return input.replaceAll(linkRegex, replacement);
   }

   /**
    * Remove usernames from the text and return a clean string.
    *
    * @param input
    *           The String to remove usernames from.
    *
    * @return A String without usernames.
    */
   public static String removeUsernames(String input) {
      String usernameRegex = "(@(\\S+))";

      return input.replaceAll(usernameRegex, "");
   }
}
