//
// StringReplace.java
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * A program to filter and replace strings in a file&mdash;in
 * other words, a poor man's pure Java version of sed.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/jar2lib/src/main/java/loci/jar2lib/StringReplace.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/jar2lib/src/main/java/loci/jar2lib/StringReplace.java">SVN</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class StringReplace {

  // -- Fields --

  private String input, output;

  // -- Constructor --

  public StringReplace(String inputPattern, String outputPattern) {
    input = inputPattern;
    output = outputPattern;
  }

  // -- StringReplace methods --

  public void processFile(String path) {
    System.out.println("Processing file: " + path);

    // read data from file
    Vector<String> lines = null;
    try {
      lines = readFile(path);
    }
    catch (IOException exc) {
      System.err.println("Error: cannot read file: " + path);
      return;
    }

    // replace patterns
    int changed = 0;
    for (int i=0; i<lines.size(); i++) {
      String line = lines.get(i);
      String newLine = line.replaceAll(input, output);
      if (!line.equals(newLine)) {
        lines.set(i, newLine);
        changed++;
      }
    }

    // write data back to file
    try {
      writeFile(path, lines);
      System.out.println(changed + " lines updated.");
    }
    catch (IOException exc) {
      System.err.println("Error: cannot write file: " + path);
      return;
    }
  }

  public Vector<String> readFile(String path) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(path));
    Vector<String> lines = new Vector<String>();
    while (true) {
      String line = in.readLine();
      if (line == null) break;
      lines.add(line);
    }
    in.close();
    return lines;
  }

  public void writeFile(String path, Vector<String> lines) throws IOException {
    File destFile = new File(path);
    File tempFile = new File(path + ".tmp");
    PrintWriter out = new PrintWriter(new FileWriter(tempFile));
    for (String line : lines) out.println(line);
    out.close();
    destFile.delete();
    tempFile.renameTo(destFile);
  }

  // -- Helper utility methods --

  public static String fixEscaped(String s) {
    s = s.replaceAll("\\\\n", "\n");
    s = s.replaceAll("\\\\r", "\r");
    s = s.replaceAll("\\\\t", "\t");
    return s;
  }

  // -- Main method --

  public static void main(String[] args) {
    if (args == null || args.length < 3) {
      System.out.println("Usage: java " + StringReplace.class.getName() +
        " inputPattern outputPattern file [file2 file3 ...]");
      return;
    }
    String inputPattern = fixEscaped(args[0]);
    String outputPattern = fixEscaped(args[1]);
    StringReplace sr = new StringReplace(inputPattern, outputPattern);
    for (int i=2; i<args.length; i++) sr.processFile(args[i]);
  }

}
