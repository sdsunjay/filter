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

//import com.spoons.control.Logger;
//import com.spoons.util.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to handle stop word related activities like removing all the
 *  stop words from a String.
 * Stopwords are marked with varying level of importance with 0 being the most
 *  frequent stopwords.
 * Not only are stopwords kept in the table, but also the stem of stopwords.
 * All calls made available from this class will be static.
 */
public final class StopWordUtils {
   /**
    * The name of the table that has all the stopwords in it.
    */
   private static final String STOPWORD_TABLE = "DATA_stopwords";

   /**
    * The replacement string palceholder for the replaced value.
    * See replaceStopWords()
    */
   private static final String REPLACE_PLACEHOLDER = "!";

   /**
    * The stopwords table.
    * Each inner Set is just a presence HashMap representing a different stop level.
    * Stopword levels decrease in importance from zero. (Zero is the most
    *  important/frequent stopwords).
    */
   private static Map<Integer, Set<String>> stopwords = loadStopWords();

   /**
    * Construct a new StopWordFilter.
    * Private to promote static behavior.
    */
   private StopWordUtils() {
   }

   /**
    * Load the stopwords from the database into memory.
    *
    * @return A HashMap representing the stopword table.
    */
   private static Map<Integer, Set<String>> loadStopWords() {
      Map<Integer, Set<String>> rtn =
       new HashMap<Integer, Set<String>>();

      String query = String.format("SELECT level, word from %s", STOPWORD_TABLE);

      List<String> words = new ArrayList<String>();
      List<Integer> levels = new ArrayList<Integer>();
/*
      try {
         //Query queryObj = new Query();
         //queryObj.doIntStringListQuery(query, levels, words);
         //queryObj.close();
      //} catch (Query.NoResultsException noResEx) {
      } catch (Exception noResEx) {
         Logger.logDebug("There are no stopwords in the database!");
         return rtn;
      } catch (Exception ex) {
         Logger.logError("Error retrieving stopwords from the databse.", ex);
         return rtn;
      }*/

      Stemmer stemmer = new Stemmer();

      for (int ndx = 0; ndx < words.size(); ndx++) {
         if (!rtn.containsKey(levels.get(ndx))) {
            rtn.put(levels.get(ndx), new HashSet<String>());
         }

         rtn.get(levels.get(ndx)).add(words.get(ndx));

         //Also put in stem
         rtn.get(levels.get(ndx)).add(stemmer.stem(words.get(ndx)));
      }

      return rtn;
   }

   public static String[] removeStopWords(List<String> input, int level) {
      return removeStopWords(input.toArray(new String[input.size()]), level);
   }

   /**
    * Return a List with all stopwords removed.
    * Only remove stopwords at the given level and below.
    * The words in input should all be lowercase.
    * Words of length two or less are automatically considered stopwords.
    *
    * @param input The list of words to analyze.
    * @param level The minimum stop level.
    *
    * @return A list of words that is like input, but without stopwords.
    *
    * @.pre level should be positive.
    */
   public static String[] removeStopWords(String[] input, int level) {
      List<String> rtn = new ArrayList<String>();
      boolean isStop;
      Set<String> levelStopWords = getStopWords(level);

      for (String word : input) {
         word = word.trim();

         isStop = false;

         if (word.length() > 2) {
            for (int ndx = 0; ndx <= level; ndx++) {
               if (levelStopWords.contains(word)) {
                  isStop = true;
                  break;
               }
            }

            if (!isStop) {
               rtn.add(word);
            }
         }
      }

      return rtn.toArray(new String[0]);
   }

   /**
    * Return a List with all stopwords replaced with the give text.
    * Only remove stopwords at the given level and below.
    * The words in input should all be lowercase.
    * Words of length two or less are automatically considered stopwords.
    * Note that running this with replacement = "" (empty string) will result
    *  in the same behavior as removeStopWords.
    *
    * As per "Cailin's Convention", any bangs ('!') in the replacement string
    *  will be replaced with the stopword.
    *  Ex: (the is a stopword) input = "the dog runs", replacement = "<!>"
    *  output = "<the> dog runs"
    *
    * @param input The list of words to analyze.
    * @param replacement The string to replace the stopword with.
    * @param level The minimum stop level.
    *
    * @return A list of words that is like input, but without stopwords.
    *
    * @.pre level should be positive.
    */
   public static List<String> replaceStopWords(String[] input,
                                               String replacement, int level) {
      List<String> rtn = new ArrayList<String>();
      String replacementStr;
      Set<String> levelStopWords = getStopWords(level);

      for (String word : input) {
         replacementStr = new String(word);

         if (word.length() > 2) {
            for (int ndx = 0; ndx <= level; ndx++) {
               if (levelStopWords.contains(word)) {
                  replacementStr =
                  replacement.replaceAll(REPLACE_PLACEHOLDER, word);
                  break;
               }
            }
         } else {
            replacementStr = replacement.replaceAll(REPLACE_PLACEHOLDER, word);
         }

         rtn.add(replacementStr);
      }

      return rtn;
   }

   public static Set<String> getStopWords(int maxLevel) {
      Set<String> stopWordsForLevel = new HashSet<String>();

      for (int level = 0; level <= maxLevel; level++) {
         Set<String> levelMap = stopwords.get(level);

         if (levelMap != null) {
            stopWordsForLevel.addAll(levelMap);
         }
      }

      return stopWordsForLevel;
   }
}
