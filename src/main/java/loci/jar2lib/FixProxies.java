//
// FixProxies.java
//

/*
Jar2Lib tool for generating C++ proxy classes for a Java library.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.jar2lib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * FixProxies is a program to post-process Jace-generated proxies.
 *
 * This step is necessary to avoid potential global namespace name clashes
 * with various Java constants.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/jar2lib/src/main/java/loci/jar2lib/FixProxies.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/jar2lib/src/main/java/loci/jar2lib/FixProxies.java">SVN</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class FixProxies {

  // -- Constants --

  private static final String PATCH_PREFIX = "JACE";
  private static final String CONSTANT_TOKEN = "CONSTANT";

  private static final String HEADER_INPUT =
    "(^.*static.* )(" + CONSTANT_TOKEN + ")\\(\\);$";
  private static final String HEADER_OUTPUT = "$1" + PATCH_PREFIX + "_$2();";
  private static final String SOURCE_INPUT =
    "(JFieldProxy.*::)(" + CONSTANT_TOKEN + ")\\(\\)$";
  private static final String SOURCE_OUTPUT = "$1" + PATCH_PREFIX + "_$2()";

  // -- Fields --

  private ArrayList<String> constants;

  // -- Constructor --

  public FixProxies(String conflictsFile)
    throws IOException
  {
    // parse list of conflicting constants
    constants = new ArrayList<String>();
    BufferedReader in = new BufferedReader(new FileReader(conflictsFile));
    while (true) {
      String line = in.readLine();
      if (line == null) break;
      line = line.trim();
      if (line.startsWith("#")) continue; // comment
      if (line.equals("")) continue; // blank line
      constants.add(line);
    }
    in.close();
  }

  // -- FixProxies methods --

  public void fixProxies(String pathPrefix) {
    for (String entry : constants) {
      int dot = entry.lastIndexOf(".");
      if (dot < 0) {
        System.err.println("Warning: invalid constant: " + entry);
        continue;
      }
      String path = entry.substring(0, dot).replaceAll("\\.", "/");
      String constant = entry.substring(dot + 1);

      // fix header file
      String headerInput = HEADER_INPUT.replaceAll(CONSTANT_TOKEN, constant);
      String headerOutput = HEADER_OUTPUT.replaceAll(CONSTANT_TOKEN, constant);
      String headerPath = pathPrefix + "/include/jace/proxy/" + path + ".h";
      new StringReplace(headerInput, headerOutput).processFile(headerPath);

      // fix source file
      String sourceInput = SOURCE_INPUT.replaceAll(CONSTANT_TOKEN, constant);
      String sourceOutput = SOURCE_OUTPUT.replaceAll(CONSTANT_TOKEN, constant);
      String sourcePath = pathPrefix + "/source/jace/proxy/" + path + ".cpp";
      new StringReplace(sourceInput, sourceOutput).processFile(sourcePath);
    }
  }

  // -- Main method --

  public static void main(String[] args) throws IOException {
    if (args == null || args.length < 2) {
      System.err.println("Usage: java " + FixProxies.class.getName() +
        " conflicts.txt /path/to/proxies [/path/to/more/proxies ...]");
      System.exit(1);
      return;
    }
    final String conflictsFile = args[0];
    final FixProxies fixProxies = new FixProxies(conflictsFile);
    for (int i = 1; i < args.length; i++) fixProxies.fixProxies(args[i]);
  }

}
