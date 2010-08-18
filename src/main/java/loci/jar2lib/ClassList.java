//
// ClassList.java
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A ClassList is a list of Java classes from a particular JAR file.
 * Note that unlike usual Java convention, the fully qualified class names
 * will be separated by forward slashes rather than dots.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/jar2lib/src/main/java/loci/jar2lib/ClassList.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/jar2lib/src/main/java/loci/jar2lib/ClassList.java">SVN</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class ClassList {

  // -- Fields --

  /** The list of classes. */
  private List<String> classes;

  // -- Constructor --

  /**
   * Constructs a list of Java classes corresponding
   * to those in the given JAR file.
   */
  public ClassList(String jarPath) throws IOException {
    classes = new ArrayList<String>();
    buildList(jarPath);
  }

  // -- ClassList API methods --

  /** Builds the list of Java classes contained in the given JAR file. */
  public synchronized void buildList(String jarPath) throws IOException {
    classes.clear();
    listFiles(new JarFile(jarPath));
    Collections.sort(classes);
  }

  /** Gets the list of Java classes. */
  public List<String> classes() {
    return classes;
  }

  // -- Helper methods --

  /** Finds all Java classes within the given JAR file. */
  private void listFiles(JarFile jarFile) {
    Enumeration<JarEntry> jarEntries = jarFile.entries();
    if (jarEntries == null) return;

    while (jarEntries.hasMoreElements()) {
      final JarEntry entry = jarEntries.nextElement();
      final String name = entry.getName();
      if (name == null) continue;
      final String lName = name.toLowerCase();
      if (!lName.endsWith(".class")) continue; // skip non-classes
      if (lName.indexOf("$") >= 0) continue; // skip inner classes
      final String className = name.substring(0, name.length() - 6);
      classes.add(className);
    }
  }

  // -- Utility methods --

  /** Gets the package name for the given fully qualified class name. */
  public static String packageName(String className) {
    int slash = className.lastIndexOf("/");
    return slash < 0 ? "" : className.substring(0, slash);
  }

  /** Returns true iff the two classes are from different packages. */
  public static boolean isNewPackage(String class1, String class2) {
    return !packageName(class1).equals(packageName(class2));
  }

  /** Gets the C++ header name corresponding to the given class name. */
  public static String header(String className) {
    return className + ".h";
  }

  /** Gets the C++ namespace fragment corresponding to the given class name. */
  public static String namespace(String className) {
    return packageName(className).replaceAll("/", "::");
  }

}
