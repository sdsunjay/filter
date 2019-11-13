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

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.HashMap;

/**
 * Parses emoticons out of tweets.
 *
 * @author Allen Dunlea and Ryan Hnarakis
 *
 */
public class EmoticonParser {
   /**
    * This string represents how the emoticon replacements will be formated.
    */
   private String format = "%s";

   /**
    * This is the mapping of emoticons to strings,
    * this is just one method that I implemented for it's ease
    * and so that testing could be done.
    */
   private HashMap <RunAutomaton, String> emoticons = null;

   /**
    *
    * @param format How the emoticon replacements will be formated.
    */
   public EmoticonParser(String format) {
      this();
      this.format = format;
   }

   /**
    *
    */
   public EmoticonParser() {
      emoticons = buildDictionary();
   }

   /**
    * Parses a single tweet by replacing all emoticons.
    *
    * @param string the tweet
    * @return a tweet with the emoticons replaced
    */
   public String parse(String string) {
      String parsedTweet = string;
      String temp = "";
      AutomatonMatcher matcher = null;
      for (RunAutomaton ra : emoticons.keySet()) {
         //parsedTweet = parsedTweet.replaceAll(emote, replaceEmoticon(emote));
         matcher = ra.newMatcher(parsedTweet);

         while (matcher.find()) {
            temp = parsedTweet.substring(0, matcher.start());
            temp = temp.concat(replaceEmoticon(ra));
            temp = temp.concat(parsedTweet.substring(matcher.end()));

            parsedTweet = temp;
            temp = "";
            matcher = ra.newMatcher(parsedTweet);
         }
      }
      return parsedTweet;
   }

   /**
    * This sets the format of the replaced emoticons.
    *
    * @param format the format to set
    */
   public void setFormat(String format) {
      this.format = format;
   }

   /**
    * @return the format
    */
   public String getFormat() {
      return format;
   }


   /**
    * Replaces an emoticon with a word using the set format (defaults to "%s").
    * @param emoticon
    * @return
    */
   protected String replaceEmoticon(RunAutomaton ra) {
      return String.format(format, emoticons.get(ra));
   }

   /**
    * Builds a basic dictionary of emoticons to words
    * The keys are finite state automaton.
    * @return
    */
   private HashMap<RunAutomaton,String> buildDictionary() {
      HashMap<RunAutomaton,String> dictionary = new HashMap<RunAutomaton, String>();

      //eyes on left smiles
      dictionary.put(new RunAutomaton(new RegExp("[:8=][ -o]?[\\)\\]>\\}D]").toAutomaton()),
                     "smile");

      //eyes on right smiles
      dictionary.put(new RunAutomaton(new RegExp("[\\(\\[<\\{C][ -o]?[:8=]").toAutomaton()),
                     "smile");

      //eyes on left angry
      dictionary.put(new RunAutomaton(new RegExp(">[:8=][ -o]?[\\(\\[<\\{o]").toAutomaton()),
                     "angry");

       //eyes on right angry
      dictionary.put(new RunAutomaton((new RegExp("[\\)\\]>\\}Do][ -o]?[:8=]\\<")).toAutomaton()),
                     "angry");

      //eyes on left frowns
      dictionary.put(new RunAutomaton(new RegExp("[:8=][ -o]?[\\(\\[\\{C]").toAutomaton()),
                     "frown");

      //eyes on right frowns
      dictionary.put(new RunAutomaton(new RegExp("[\\)\\]\\}D][ -o]?[:8=]").toAutomaton()),
                     "frown");

      //eyes on left smiles
      dictionary.put(new RunAutomaton(new RegExp(";[ -o]?[\\)\\]>\\}D]").toAutomaton()), "wink");

      //eyes on right winks
      dictionary.put(new RunAutomaton(new RegExp("[\\(\\[<\\{][ -o]?;").toAutomaton()), "wink");

      //eyes on left smiles
      dictionary.put(new RunAutomaton(new RegExp("[:8=][ ]?[\\\\/]").toAutomaton()), "slant");

      //eyes on right winks
      dictionary.put(new RunAutomaton(new RegExp("[\\\\/][ ]?[:=8]").toAutomaton()), "slant");

      //heart
      dictionary.put(new RunAutomaton(new RegExp("\\<3").toAutomaton()), "heart");

      //eastern faces
      dictionary.put(new RunAutomaton(new RegExp(">.>").toAutomaton()), "shifty");
      dictionary.put(new RunAutomaton(new RegExp("\\<.\\<").toAutomaton()), "shifty");

      dictionary.put(new RunAutomaton(new RegExp("\\^.\\^").toAutomaton()), "happy");

      dictionary.put(new RunAutomaton(new RegExp(">.\\<").toAutomaton()), "doh");

      return dictionary;
   }
}
