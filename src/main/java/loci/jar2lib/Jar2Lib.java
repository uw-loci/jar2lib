//
// Jar2Lib.java
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

import jace.proxy.AutoProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Generates a C++ project for C++ proxies corresponding
 * to a list of Java JAR files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/jar2lib/src/main/java/loci/jar2lib/Jar2Lib.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/jar2lib/src/main/java/loci/jar2lib/Jar2Lib.java">SVN</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class Jar2Lib {

  // -- Constants --

  public static final String DEFAULT_OUTPUT_DIR = "jar2lib-output";

  // -- Fields --

  private List<String> jarPaths;
  private String conflictsPath;
  private String headerPath;
  private String outputPath;

  // -- Constructor --

  public Jar2Lib() {
    jarPaths = new ArrayList<String>();
    outputPath = DEFAULT_OUTPUT_DIR;
  }

  // -- Jar2Lib methods --

  public void parseArgs(String[] args) {
    jarPaths = new ArrayList<String>();
    conflictsPath = null;
    headerPath = null;
    outputPath = DEFAULT_OUTPUT_DIR;
    for (int i = 0; i < args.length; i++) {
      final String arg = args[i];
      if (arg.equals("-conflicts")) {
        if (i == args.length - 1) die("Error: no conflicts file given.");
        conflictsPath = args[++i];
      }
      else if (arg.equals("-header")) {
        if (i == args.length - 1) die("Error: no header file given.");
        headerPath = args[++i];
      }
      else if (arg.equals("-output")) {
        if (i == args.length - 1) die("Error: no output path given.");
        outputPath = args[++i];
      }
      else if (arg.startsWith("-")) die("Unknown flag: " + arg);
      else jarPaths.add(arg);
    }
    if (jarPaths.size() == 0) {
      die("Usage: java " + getClass().getName() +
        " library.jar [library2.jar ...]\n" +
				"  [-conflicts conflicts.txt] [-header header.txt]\n" +
				"  [-output /path/to/output-project]");
    }
  }

  public void execute() throws IOException, VelocityException {
    // check conflicts file
    if (conflictsPath != null && !new File(conflictsPath).exists()) {
      throw new IllegalStateException("Invalid conflicts file: " +
        conflictsPath);
    }

    // check header file
    if (headerPath != null && !new File(headerPath).exists()) {
      throw new IllegalStateException("Invalid header file: " + headerPath);
    }

    // create output directory
    final File outputDir = new File(outputPath);
    if (!outputDir.exists()) outputDir.mkdirs();
    if (!outputDir.isDirectory()) {
      throw new IllegalStateException("Not a valid directory: " + outputPath);
    }

    // generate jace headers
    final VelocityAutogen generator = new VelocityAutogen(headerPath);
    final File includeDir = new File(outputPath, "include");
		if (!includeDir.exists()) includeDir.mkdirs();
    for (String jarPath : jarPaths) {
			final File jarFile = new File(jarPath);
      log("--> Generating header for " + jarFile.getName());
      generator.createJaceHeader(jarPath, path(includeDir));
    }
    log("--> Generating CMake build file");
		final String projectId = "bfcpp"; //TODO TEMP!
		final String projectName = "Bio-Formats C++ bindings"; //TODO TEMP!
		generator.createCMakeLists(projectId, projectName, path(outputDir));

    // copy resources to output directory
    log("--> Copying resources");
    final List<String> jaceResources = findResources("jace/");
    for (String resource : jaceResources) copyResource(resource, outputDir);

    // generate proxies using jace
    final File sourceDir = new File(outputPath, "source");
		if (!sourceDir.exists()) sourceDir.mkdirs();
    final File proxiesDir = new File(outputPath, "proxies");
    final File proxiesIncludeDir = new File(proxiesDir, "include");
		if (!proxiesIncludeDir.exists()) proxiesIncludeDir.mkdirs();
    final File proxiesSourceDir = new File(proxiesDir, "source");
		if (!proxiesSourceDir.exists()) proxiesSourceDir.mkdirs();
    final String osName = System.getProperty("os.name");
    final boolean isWindows = osName.indexOf("Windows") >= 0;
    final List<String> autoProxyArgs = new ArrayList<String>();
    autoProxyArgs.add(path(includeDir));
    autoProxyArgs.add(path(sourceDir));
    autoProxyArgs.add(path(proxiesIncludeDir));
    autoProxyArgs.add(path(proxiesSourceDir));
    autoProxyArgs.add(classpath(jarPaths));
    autoProxyArgs.add("-mindep");
    if (isWindows) autoProxyArgs.add("-exportsymbols");
    log("--> Generating proxies");
    AutoProxy.main(autoProxyArgs.toArray(new String[0]));

    // post-process proxies to resolve any conflicts
    if (conflictsPath != null) {
      log("--> Renaming conflicting constants");
      final FixProxies fixProxies = new FixProxies(conflictsPath);
      fixProxies.fixProxies(path(proxiesDir));
    }

    // TODO - print instructions on how to proceed with CMake
    // TODO - copy "final product" files such as wrapped JARs to build dir

    log("--> Done");
  }

  // -- Main method --

  public static void main(String[] args) throws Exception {
    final Jar2Lib jar2Lib = new Jar2Lib();
    try {
      jar2Lib.parseArgs(args);
    }
    catch (IllegalArgumentException exc) {
      System.err.println(exc.getMessage());
      System.exit(1);
    }
    try {
      jar2Lib.execute();
    }
    catch (IllegalStateException exc) {
      System.err.println(exc.getMessage());
      System.exit(2);
    }
  }

  // -- Helper methods --

  private void log(String message) {
    // TODO - improving logging mechanism?
    System.out.println(message);
  }

  private void die(String message) {
    throw new IllegalArgumentException(message);
  }

  /** Gets the absolute path to the given file, with forward slashes. */
  private String path(File file) {
    final String path = file.getAbsolutePath();
    // NB: Use forward slashes even on Windows.
    return path.replaceAll(File.separator, "/");
  }

  /** Builds a classpath corresponding to the given list of JAR files. */
  private String classpath(List<String> jarPaths)
    throws UnsupportedEncodingException
  {
    final StringBuilder sb = new StringBuilder();
    final String jrePath = findRuntime();
    sb.append(jrePath);
    for (String jarPath : jarPaths) {
      sb.append(":");
      sb.append(jarPath);
    }
    return sb.toString();
  }

  /** Locates the JAR file containing this JVM's classes. */
  private String findRuntime() throws UnsupportedEncodingException {
    return findEnclosingJar(Object.class);
  }

  /** Scans the enclosing JAR file for all resources beneath the given path. */
  private List<String> findResources(String resourceDir) throws IOException {
    final List<String> resources = new ArrayList<String>();
    final String jarPath = findEnclosingJar(getClass());
    final JarFile jarFile = new JarFile(jarPath);
    final Enumeration<JarEntry> jarEntries = jarFile.entries();
    if (jarEntries != null) {
      while (jarEntries.hasMoreElements()) {
        final JarEntry entry = jarEntries.nextElement();
        final String name = entry.getName();
        if (name == null) continue;
        if (name.startsWith(resourceDir)) resources.add(name);
      }
    }
    return resources;
  }

  /** Copies the given resource to the specified output directory. */
  private void copyResource(String resource, File baseDir)
    throws IOException
  {
		log(resource);
    final File outputFile = new File(baseDir, resource);
    final File outputDir = outputFile.getParentFile();
    if (!outputDir.exists()) outputDir.mkdirs();
		if (resource.endsWith("/")) {
			// resource is a directory
			outputFile.mkdir();
		}
		else {
			// resource is a file
			final InputStream in = getClass().getResourceAsStream("/" + resource);
			final OutputStream out = new FileOutputStream(outputFile);
			final byte[] buf = new byte[512 * 1024]; // 512K buffer
			while (true) {
				int r = in.read(buf);
				if (r <= 0) break; // EOF
				out.write(buf, 0, r);
			}
			out.close();
			in.close();
		}
  }

	/**
	 * Finds the JAR file (or file system path)
	 * from which the given class was loaded.
	 */
  public static String findEnclosingJar(Class<?> c)
    throws UnsupportedEncodingException
  {
    String className = c.getName();
    int dot = className.lastIndexOf(".");
    if (dot >= 0) className = className.substring(dot + 1);
    String path = c.getResource(className + ".class").toString();
    path = path.replaceAll("^jar:", "");
    path = path.replaceAll("^file:", "");
    path = path.replaceAll("^/*/", "/");
    path = path.replaceAll("^/([A-Z]:)", "$1");
    path = path.replaceAll("!.*", "");
    path = URLDecoder.decode(path, "UTF-8");
    String slash = File.separator;
    if (slash.equals("\\")) slash = "\\\\";
    path = path.replaceAll("/", slash);
    return path;
  }

}
