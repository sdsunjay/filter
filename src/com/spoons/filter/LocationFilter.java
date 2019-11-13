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
//import com.spoons.control.MasterControl;
//import com.spoons.util.Query;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is capable of parsing movie/show locations from a String and
 *  replacing them with some text.
 *
 * Make sure init() is called before using the LocationFilter.
 * Control will usually take care of this.
 *
 * This class will statically (on class load) pull all the know locations from
 *  the database. It will them build a structures based around hashmaps for
 *  quick location lookup. Common single word locations will be removed. These locations will
 *  be discovered using the document frequency table.
 *
 * Location Data Structure:
 *  (It's a Trie)
 *  The structure will be a hash of Nodes.
 *  Each node will indicate wheither or not it is terminating (location may stop there),
 *   and it will contain a Hash to the next node in the location keyed by word.
 *  All punctuation will be removed from the locations and the location will be lowercased
 *   before being put in the structure.
 *   SmartSplitSting.split() will be used for that.
 *  The LocationFilter will search for the possible longest match.
 *
 * TODO(eriq): Use/update/make the document frequencey table.
 */
public class LocationFilter {
   /**
    * The table that houses the locations.
    */
   private static final String LOCATION_TABLE = "DATA_cities_original";

   /**
    * The table that has the word frequencies.
    */
   private static final String WORD_FREQ_TABLE = "CALC_word_frequency";

   /**
    * The default replacement string.
    */
   private static final String DEFAULT_REPLACE = "<$location$>";

   /**
    * The minimum frequency necessary to not include for single location.
    */
   private static final int MIN_location_FREQ = 5000;

   /**
    * The structure that holds all the locations.
    */
   private static Map<String, Node> locationsStruct = null;

    /**
    * The structure that holds all replacedLocations
    */
   private static ArrayList<String> replacedLocations = null;

   /**
    * Just a testing main.
    */
   public static void main(String[] args) {
        // MasterControl.init("config/empty.properties");
      init();

      //Print the entire Location structure.
      System.out.println(locationsToString());
   }

   /**
    * Call this before using the LocationFilter.
    * This will initialize the location structure.
    */
   public static synchronized void init() {
      if (locationsStruct == null) {
         locationsStruct = createLocationsStructure();
      }
        replacedLocations = new ArrayList<String>();
   }

   /**
    * Get the entire location structure as a String.
    */
   public static String locationsToString() {
      String rtn = "";

      for (Map.Entry<String, Node> entry : locationsStruct.entrySet()) {
         rtn += (entry.getKey() + "(" + entry.getValue().isLocation + ")\n");
         rtn += nodeToString(entry.getValue(), "   ");
      }

      return rtn;
   }
  public static ArrayList<String> getReplacedLocations(){
    return replacedLocations;
  }
   /**
    * Recursive toString for Nodes.
    */
   private static String nodeToString(Node node, String indent) {
      if (node == null) {
         return "";
      }

      String rtn = "";
      for (Map.Entry<String, Node> entry : node.nextWords.entrySet()) {
         rtn += (indent + entry.getKey() + "(" + entry.getValue().isLocation + ")\n");
         rtn += nodeToString(entry.getValue(), indent + "   ");
      }

      return rtn;
   }

   /**
    * Parse the text for locations and replace them with the default replacement
    *  String.
    * It is assumed that the given text has been split with SmartSplitString.split().
    */
   public static String[] replaceLocations(String[] words) {
       /*for (int i=0;i<words.length;i++)
       {
           System.out.println(words[i]);
       }*/
       return replacelocations(words, DEFAULT_REPLACE);
   }

   /**
    * Parse the text for locations and replace them with the given String.
    * It is assumed that the given text has been split with SmartSplitString.split().
    * If you want a less accurate but faster method, try singlePassReplacelocations().
    */
   public static String[] replacelocations(String[] words, String replace) {
      while (true) {
         String[] tempWords = singlePassReplacelocations(words, replace);
         if (tempWords == null) {
             if(!replacedLocations.isEmpty()){
                 System.out.println("Replaced: ");
                 for (String s : replacedLocations){
                    System.out.println(s);
                 }
             }
             return words;
         }

         words = tempWords;
      }
   }

   /**
    * Like replacelocations(), but make at most one replacement and will return null if
    *  no replacement is made.
    * Makes the greedy replacement.
    * This can be used for speed.
    *
    * @return The Strings with a single location replace, or null.
    */
   public static String[] singlePassReplacelocations(String[] words, String replace) {
      int matchLen = 0;
      int matchNdx = -1;

      StringBuilder replacedLocation = new StringBuilder();

      for (int i = 0; i < words.length; i++) {
         matchLen = longestMatch(words, i);
         if (matchLen != 0) {
            matchNdx = i;
            break;
         }
      }

      if (matchLen == 0) {
         return null;
      }
      //A location does begin with a word
      for(int numOfWords = matchNdx; numOfWords < matchLen + matchNdx; numOfWords++) {
         if (replacedLocation.length() > 0) {
            replacedLocation.append(" ");
         }
         replacedLocation.append(words[numOfWords]);
      }
        replacedLocations.add(replacedLocation.toString());

      String[] rtn = new String[words.length - matchLen + 1];
      int j = 0;
      for (int i = 0; i < words.length; i++) {
         if (i < matchNdx || i >= (matchNdx + matchLen)) {
            rtn[j] = words[i];
            j++;
         // Only put the location in on the exact matchNdx, just do nothing
         //  nothing on the other ndxs.
         } else if (i == matchNdx) {
            rtn[j] = replace;
            j++;
         }
      }

      return rtn;
   }

   /**
    * Check to see if the substring starting at ndx is a location.
    *
    * @return the length of the longest location, 0 if no match.
    */
   private static int longestMatch(String[] words, int ndx) {
      if (ndx >= words.length) {
         return 0;
      }

      int count = 0;
      int longestMatch = 0;
      Node currentNode = locationsStruct.get(words[ndx]);

      //Print the entire location structure.
      //System.out.println(locationsToString());
      while (currentNode != null) {
          count++;
          ndx++;

          if (currentNode.isLocation) {
              longestMatch = count;
          }

          if (ndx >= words.length) {
              break;
          }

          currentNode = currentNode.nextWords.get(words[ndx]);
      }
      return longestMatch;
}

/**
 * Create the structure that holds all the locations and fill it up.
 */
private static Map<String, Node> createLocationsStructure() {
    Map<String, Node> rtn = new HashMap<String, Node>();
    List<String> locations = getlocations();
    if (locations.isEmpty()) {
        System.err.println("No locations loaded");
    }
    for (String location : locations) {
        String[] words = SmartSplitString.split(location);

        // Ignore short locations, they are too dangerous
        // TODO(eriq): Do better with short locations.
        /*if (words.length <= 3) {
            continue;
        }*/

        rtn.put(words[0], insertLocation(rtn.get(words[0]), words, 0));
    }

    return rtn;
}

/**
 * Insert the location into the structure starting at the word with the given index.
 */
private static Node insertLocation(Node currentLocation, String[] words, int ndx) {
    if (ndx == words.length) {
        // This should not happen.
        Logger.logError("Undesired case in inserting locations.");
        return null;
    }

    if (currentLocation == null) {
        Node rtn = null;
        if (ndx == (words.length - 1)) {
            rtn = new Node(true);
        } else {
            rtn = new Node(false);
            rtn.nextWords.put(words[ndx + 1], insertLocation(null, words, ndx + 1));
        }

        return rtn;
    }

    // Location is finished, but is repeat or substring of another location
    if (ndx == (words.length - 1)) {
        currentLocation.isLocation = true;
        return currentLocation;
    }

    // This handles both cases where nextWords has and does not
    //  already have words[ndx] as a key.
    currentLocation.nextWords.put(words[ndx + 1], insertLocation(
                currentLocation.nextWords.get(words[ndx + 1]), words, ndx + 1));

    return currentLocation;
}

/**
 * Get known locations from the database, ignoring common single word locations.
 *
 * @return The raw locations.
 *
 * @TODO(eriq): Ignore the common single words.
 */
private static List<String> getlocations() {
    List<String> rtn;
    //String query = String.format("SELECT location FROM %s " +
    // "WHERE location NOT IN (SELECT word FROM %s WHERE count > %d)",
    // location_TABLE, WORD_FREQ_TABLE, MIN_location_FREQ);
    //Query queryObj = new Query();

    try {
        //rtn = queryObj.doStringListQuery(query);

        rtn = ReadFile.readFile("allCities.txt");

    } catch (Exception ex) {
        Logger.logError("Error getting the locations.", ex);
        rtn = new ArrayList<String>();
    }
    /*finally {
      queryObj.close();
      }*/

    return rtn;
}

private static class Node {
    /**
     * Whether or not this specific Node is a location.
     */
    public boolean isLocation;

    /**
     * The locations that use this word and continue.
     */
    public Map<String, Node> nextWords;

    public Node(boolean isLocation) {
        this.isLocation = isLocation;
        nextWords = new HashMap<String, Node>();
    }
}
}
