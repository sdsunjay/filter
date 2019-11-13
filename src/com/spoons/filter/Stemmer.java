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

import org.tartarus.snowball.SnowballStemmer;

/**
 * A simple wrapper around the Tartarus Snowball stemmer.
 */
public class Stemmer {
   /**
    * The Porter's stemmer to use.
    */
   private SnowballStemmer stem;

   public Stemmer() {
      stem = new org.tartarus.snowball.ext.porterStemmer();
   }

   /**
    * Stem a single word using Proter's algorithm.
    *
    * @param word The single word to stem.
    *
    * @return A new String containing the stemmed version of the word.
    */
   public String stem(String word) {
      stem.setCurrent(word);
      stem.stem();

      return stem.getCurrent();
   }

   /*
    * A static variant of the stemmer.
    * Slower because of overhead.
    */
   public static String staticStem(String word) {
      Stemmer stemmer = new Stemmer();
      return stemmer.stem(word);
   }

   /**
    * A Testing main used to check the behavior of the stemmer.
    *
    * @param args Ignored.
    */
   public static void main(String[] args) {
      SnowballStemmer stem = new org.tartarus.snowball.ext.porterStemmer();

      stem.setCurrent("watching");
      stem.stem();
      System.out.println(stem.getCurrent());

      stem.setCurrent("emotzsmilez");
      stem.stem();
      System.out.println(stem.getCurrent());

      stem.setCurrent("emotzfrownz");
      stem.stem();
      System.out.println(stem.getCurrent());

      stem.setCurrent("dave's");
      stem.stem();
      System.out.println(stem.getCurrent());

      stem.setCurrent("high-er");
      stem.stem();
      System.out.println(stem.getCurrent());

      stem.setCurrent("<watching>");
      stem.stem();
      System.out.println(stem.getCurrent());
   }
}
