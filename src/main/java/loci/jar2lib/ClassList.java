/*
 * #%L
 * Jar2lib tool for generating C++ proxy classes for a Java library.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
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
 * @author Curtis Rueden
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

      // TEMP - workaround for Jace bug
      if (className.indexOf("_") >= 0) continue;

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
