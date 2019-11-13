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
 * The sole purpose of this class is to split a string along what we define as a word.
 */
public class SmartSplitString {

   /**
    * Construct a new SmartSplitString.
    * Private to promote static behavior.
    */
   private SmartSplitString() {
   }

   /**
    * Default split that ignores meta words.
    */
   public static String[] split(String text) {
      return split(text, false);
   }

   /**
    * Split a string into an array of Strings.
    * No Stemming will occur, but all non-alpha characters will
    *  be taken out.
    * Preserve words inside unescaped "<$$>".
    *
    * @param text The String to split up. This will be modified.
    *
    * @return The list of words in text.
    */
   public static String[] split(String text, boolean replaceMeta) {
      List<String> tempWords = new ArrayList<String>();

      int index = 0;
      boolean inMeta = false;
      boolean inWord = false;
      String word = "";

      while (index < text.length()) {
         char currentChar = Character.toLowerCase(text.charAt(index));

         boolean hasNext = index < (text.length() - 1);
         char nextChar = '\0';
         if (hasNext) {
            nextChar = Character.toLowerCase(text.charAt(index + 1));
         }

         boolean hasPrev = index > 0;
         char prevChar = '\0';
         if (hasPrev) {
            prevChar = Character.toLowerCase(text.charAt(index - 1));
         }

         if (Character.isWhitespace(currentChar)) {
            if (inMeta) {
               word += currentChar;
               inWord = true;
            } else if (inWord) {
               addWord(tempWords, word, replaceMeta);
               word = "";

               inWord = false;
            }
         // If we are replaceing metawords, # and @ get special treatment.
         } else if (!inMeta && !inWord && replaceMeta &&
                    (currentChar == '@' || currentChar == '#') &&
                    hasNext && 'a' <= nextChar && nextChar <= 'z' &&
                    (!hasPrev || (hasPrev && Character.isWhitespace(prevChar)))) {
            if (currentChar == '#') {
               addWord(tempWords, "<$#$>", replaceMeta);
            } else {
               addWord(tempWords, "<$@$>", replaceMeta);
            }
         // Pay special attention to apostraphies.
         } else if (hasNext &&
                    inWord &&
                    (currentChar == '\'' && ('a' <= nextChar && nextChar <= 'z'))) {
            // Just drop the apostraphe
            word += nextChar;
            index++;
         } else if (hasNext && (currentChar == '<' && nextChar == '$')) {
            //Dump the previous word, it is not a meta word
            if (inWord) {
               addWord(tempWords, word, replaceMeta);
               word = "";
            }

            word += "<$";
            inWord = true;
            inMeta = true;
            // Move an extra step ahead
            index++;
         } else if (inMeta && hasNext && (currentChar == '$' && nextChar == '>')) {
            word += "$>";
            addWord(tempWords, word, replaceMeta);
            word = "";
            inWord = false;
            inMeta = false;
            // Move an extra step ahead
            index++;
         } else {
            if (inMeta || ('a' <= currentChar && currentChar <= 'z')) {
               word += currentChar;
               inWord = true;
            } else if (inWord && !('a' <= currentChar && currentChar <= 'z')) {
               addWord(tempWords, word, replaceMeta);
               word = "";
               inWord = false;
            }
         }

         index++;
      }

      if (word.length() > 0) {
         addWord(tempWords, word, replaceMeta);
      }

      return tempWords.toArray(new String[0]);
   }

   /**
    * Add a word to the list of words.
    * This abstraction is a great place to check all the words that go into the list.
    */
   private static void addWord(List<String> words, String word, boolean replaceMeta) {
      if (replaceMeta) {
         // Re-Tweets get replaced
         if (word.equals("rt")) {
            words.add("<$RT$>");
         } else {
            words.add(word);
         }
      } else {
         words.add(word);
      }
   }
}
