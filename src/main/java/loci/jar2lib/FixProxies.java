//
// FixProxies.java
//

/*
Jar2Lib tool for generating C++ proxy classes for a Java library.
Copyright (C) 2010-@year@ UW-Madison LOCI.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.jar2lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    BufferedReader in = new BufferedReader(new InputStreamReader(
      FixProxies.class.getResourceAsStream(conflictsFile)));
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
    }
    final String conflictsFile = args[0];
    final FixProxies fixProxies = new FixProxies(conflictsFile);
    for (int i = 1; i < args.length; i++) fixProxies.fixProxies(args[i]);
  }

}
