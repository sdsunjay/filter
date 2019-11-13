/*
 * com.spoons.filter
 *
 * Copyright (c) 2012-2019 Eriq Augustine and Sunjay Dhama
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
import java.io.File;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;
/**
 * A Filter that removes stop words, replaces links and emotes, and does stemming.
 * It does pretty much everything but Location replacement, and only because that takes longer.
 *
 * Order:
 *  Links
 *  Emotes
 *  Smart Split (Also remove Punct)
 *  Locations
 *  Stop words
 *
 * Right now it is called Eriq Filter because I can't think up a better name.
 */
public class EriqFilter extends TextFilter {
  private static EmoticonParser emote = new EmoticonParser(" <$emote:%s$> ");

  private boolean replaceMetaWords;

  // Convience main for testing/fun/writing papers.
  public static void main(String[] args) {
    //SingleControl.init("config/empty.properties");

    //      String test1 = "RT @pawlooza: ... wished @netflix had The Littlest Hobo." +
    //" If you're gonna launch in rolling hills estate, know your demographic :)" +
    // " http://bit.ly/WbcrU";
    //    String test2 = "RT @pawlooza: ... wished @netflix had The Littlest Hobo. oakland, oakley, San Porto, Santa Cruz, South Lake Sunjay, Orange something really long";

    String inputFilename = "testPosts.txt";
    String outputFilename = "output.txt";
    TextFilter eriqFilter = new EriqFilter();
    Writer writer = null;

    try {
      File outputFile = new File(outputFilename);
      outputFile.createNewFile(); // if file already exists will do nothing
      FileOutputStream oFile = new FileOutputStream(outputFile, false);
      writer = new BufferedWriter(new OutputStreamWriter(oFile, "utf-8"));
      FileInputStream inputStream = null;
      Scanner sc = null;
      String line = null;
      try {
        inputStream = new FileInputStream(inputFilename);
        sc = new Scanner(inputStream, "UTF-8");

        while (sc.hasNextLine()) {
           line = sc.nextLine();
           writer.write(eriqFilter.filter(line) + "\n");
          // System.out.println(line);
        }
        // note that Scanner suppresses exceptions
        if (sc.ioException() != null) {
          throw sc.ioException();
        }
      } catch (IOException ex) {
        System.err.println("An error occurred with reading from the file");
      } finally {
        if (inputStream != null) {
          inputStream.close();
        }
        if (sc != null) {
          sc.close();
        }
      }
    } catch (IOException ex) {
      System.err.println("An error occurred with outputting to file");
    } finally {
      try {writer.close();} catch (Exception ex) {/*ignore*/}
    }
  }

  public EriqFilter(boolean replaceMetaWords) {
    super();
    this.replaceMetaWords = replaceMetaWords;
  }

  public EriqFilter() {
    this(true /* replace meta words */);
  }

  /**
   * @inheritDoc
   */
  public String[] splitFilter(String input) {
    input = MiscFilters.replaceLinks(input, " <$link$> ");
    input = emote.parse(input);

    String[] allWords = SmartSplitString.split(input, replaceMetaWords);
    LocationFilter.init();
    allWords = LocationFilter.replaceLocations(allWords);
    allWords = StopWordUtils.removeStopWords(allWords, 0);

    return allWords;
  }

  public String toString() {
    return super.toString() + "{Replace Meta Words = " + replaceMetaWords + "}";
  }
}
