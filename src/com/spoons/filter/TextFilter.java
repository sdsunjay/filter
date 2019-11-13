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

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for a text filter.
 * Filters will take a string and return a new string that has been
 *  filtered.
 */
public abstract class TextFilter {
   /**
    * Filter a string.
    */
   public String filter(String input) {
      String rtn = "";
      String[] words = splitFilter(input);

      for (String word : words) {
         rtn += word + " ";
      }

      return rtn.replaceFirst(" $", "");
   }

   /**
    * Filter and split a string.
    * This is where the core functionality is.
    */
   public abstract String[] splitFilter(String input);

   /**
    * Filter many strings.
    */
   public List<String> filter(List<String> input) {
      List<String> rtn = new ArrayList<String>();

      for (String str : input) {
         rtn.add(filter(str));
      }

      return rtn;
   }

   public List<String[]> splitFilter(List<String> input) {
      List <String[]> rtn = new ArrayList<String[]>();

      for (String str : input) {
         rtn.add(splitFilter(str));
      }

      return rtn;
   }

   public String toString() {
      return this.getClass().getCanonicalName();
   }
}
