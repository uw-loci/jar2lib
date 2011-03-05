//
// Jar2Lib.java
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

import jace.proxy.AutoProxy;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
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

  private static final String RESOURCE_PREFIX = "project-files/";

  // -- Fields --

  private String projectId;
  private String projectName;
  private List<String> libraryJars;
  private List<String> classpathJars;
  private String conflictsPath;
  private String headerPath;
  private String sourcePath;
  private String outputPath;

  private File[] sourceFiles;
  private File outputDir;
  private File outputIncludeDir, outputSourceDir;
  private File proxiesDir;
  private File proxiesIncludeDir, proxiesSourceDir;

  // -- Constructor --

  public Jar2Lib() {
    libraryJars = new ArrayList<String>();
    classpathJars = new ArrayList<String>();
  }

  // -- Jar2Lib methods --

  public String getProjectId() {
    return projectId;
  }
  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }
  public String getProjectName() {
    return projectName;
  }
  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }
  public List<String> getLibraryJars() {
    return libraryJars;
  }
  public void setLibraryJars(List<String> libraryJars) {
    this.libraryJars = libraryJars;
  }
  public List<String> getClasspathJars() {
    return classpathJars;
  }
  public void setClasspathJars(List<String> classpathJars) {
    this.classpathJars = classpathJars;
  }
  public String getConflictsPath() {
    return conflictsPath;
  }
  public void setConflictsPath(String conflictsPath) {
    this.conflictsPath = conflictsPath;
  }
  public String getHeaderPath() {
    return headerPath;
  }
  public void setHeaderPath(String headerPath) {
    this.headerPath = headerPath;
  }
  public String getSourcePath() {
    return sourcePath;
  }
  public void setSourcePath(String sourcePath) {
    this.sourcePath = sourcePath;
  }
  public String getOutputPath() {
    return outputPath;
  }
  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  /** Parses the settings from the given command line arguments. */
  public void parseArgs(String[] args) {
    projectId = args.length >= 1 ? args[0] : null;
    projectName = args.length >= 2 ? args[1] : null;
    libraryJars = new ArrayList<String>();
    classpathJars = new ArrayList<String>();
    conflictsPath = null;
    headerPath = null;
    outputPath = null;
    for (int i = 2; i < args.length; i++) {
      final String arg = args[i];
      if (arg.equals("-conflicts")) {
        if (i == args.length - 1) die("Error: no conflicts file given.");
        conflictsPath = args[++i];
      }
      else if (arg.equals("-header")) {
        if (i == args.length - 1) die("Error: no header file given.");
        headerPath = args[++i];
      }
      else if (arg.equals("-source")) {
        if (i == args.length - 1) die("Error: no source path given.");
        sourcePath = args[++i];
      }
      else if (arg.equals("-output")) {
        if (i == args.length - 1) die("Error: no output path given.");
        outputPath = args[++i];
      }
      else if (arg.startsWith("-")) die("Unknown flag: " + arg);
      else libraryJars.add(arg);
    }
    if (projectId == null || projectName == null || libraryJars.size() == 0) {
      die("Usage: java " + getClass().getName() + " projectId projectName\n" +
        "  library.jar [library2.jar ...]\n" +
        "  [-conflicts conflicts.txt] [-header header.txt]\n" +
        "  [-output /path/to/output-project]");
    }
    if (outputPath == null) outputPath = projectId;
  }

  /** Generates a C++ wrapper project based on the current settings. */
  public void execute() throws IOException, VelocityException {
    validateInputs();
    generateSkeleton();
    copySourceFiles();
    generateHeaders();
    generateProxies();
    fixConflicts();

    // TODO - print instructions on how to proceed with CMake
    // TODO - copy "final product" files such as wrapped JARs to build dir

    log("--> Done");
  }

  /**
   * Checks that the current settings for project production are valid.
   * Creates various needed output directories if needed.
   *
   * @throws IllegalStateException if the settings are invalid.
   */
  public void validateInputs() {
    // check project ID
    if (projectId == null || !projectId.matches("^(\\w)([\\w\\-])*$")) {
      throw new IllegalStateException("Invalid project ID: " + projectId);
    }

    // check project name
    if (projectName == null) {
      throw new IllegalStateException("Invalid project name: " + projectName);
    }

    // check conflicts file
    if (conflictsPath != null && !new File(conflictsPath).exists()) {
      throw new IllegalStateException("Invalid conflicts file: " +
        conflictsPath);
    }

    // check header file
    if (headerPath != null && !new File(headerPath).exists()) {
      throw new IllegalStateException("Invalid header file: " + headerPath);
    }

    // generate list of source files
    sourceFiles = listSourceFiles(sourcePath);

    // create output directory
    outputDir = new File(outputPath);
    if (!outputDir.exists()) outputDir.mkdirs();
    if (!outputDir.isDirectory()) {
      throw new IllegalStateException("Not a valid directory: " + outputPath);
    }

    // create source and include directories
    outputIncludeDir = new File(outputDir, "include");
    if (!outputIncludeDir.exists()) outputIncludeDir.mkdirs();
    outputSourceDir = new File(outputDir, "source");
    if (!outputSourceDir.exists()) outputSourceDir.mkdirs();

    proxiesDir = new File(outputPath, "proxies");
    proxiesIncludeDir = new File(proxiesDir, "include");
    if (!proxiesIncludeDir.exists()) proxiesIncludeDir.mkdirs();
    proxiesSourceDir = new File(proxiesDir, "source");
    if (!proxiesSourceDir.exists()) proxiesSourceDir.mkdirs();
  }

  /**
   * Generates one header per input Java library.
   * Also generates the CMake build file.
   */
  public void generateHeaders() throws IOException, VelocityException {
    final VelocityAutogen generator = new VelocityAutogen(headerPath);
    for (String jarPath : libraryJars) {
      final File jarFile = new File(jarPath);
      log("--> Generating header for " + jarFile.getName());
      generator.createJaceHeader(jarPath, path(outputIncludeDir));
    }
    log("--> Generating CMake build file");
    generator.createCMakeLists(projectId, projectName,
      sourceFiles, path(outputDir));
  }

  /**
   * Copies static project resources into the project directory.
   * In particular, copies the Jace C++ distribution and related files.
   */
  public void generateSkeleton() throws IOException {
    log("--> Generating project skeleton");
    final List<String> projectResources = findResources(RESOURCE_PREFIX);
    for (String resource : projectResources) {
      final String outPath = resource.substring(RESOURCE_PREFIX.length());
      if (outPath.equals("")) continue; // skip base folder
      copyResource(resource, outPath);
    }
  }

  /** Copies any extra C++ source files into the project directory. */
  public void copySourceFiles() throws IOException {
    if (sourceFiles.length == 0) return; // no sources
    log("--> Copying sources");
    for (File sourceFile : sourceFiles) {
      copySourceFile(sourceFile);
    }
  }


  /** Generates the C++ proxy classes enumerated in the C++ headers. */
  public void generateProxies() throws UnsupportedEncodingException {
    final String osName = System.getProperty("os.name");
    final boolean isWindows = osName.indexOf("Windows") >= 0;
    final List<String> autoProxyArgs = new ArrayList<String>();
    autoProxyArgs.add(path(outputIncludeDir));
    autoProxyArgs.add(path(outputSourceDir));
    autoProxyArgs.add(path(proxiesIncludeDir));
    autoProxyArgs.add(path(proxiesSourceDir));
    List<String> allJars = new ArrayList<String>();
    allJars.addAll(libraryJars);
    allJars.addAll(classpathJars);
    autoProxyArgs.add(classpath(allJars));
    autoProxyArgs.add("-mindep");
    if (isWindows) autoProxyArgs.add("-exportsymbols");
    log("--> Generating proxies");
    AutoProxy.main(autoProxyArgs.toArray(new String[0]));
  }

  /**
   * Post-processes the generated proxies to correct any
   * conflicts identified in the specified conflicts file.
   */
  public void fixConflicts() throws IOException {
    if (conflictsPath == null) return;
    log("--> Renaming conflicting constants");
    final FixProxies fixProxies = new FixProxies(conflictsPath);
    fixProxies.fixProxies(path(proxiesDir));
  }

  // -- Main method --

  public static void main(final String[] args) throws Exception {
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

  protected void log(final String message) {
    System.out.println(message);
  }

  protected void die(final String message) {
    throw new IllegalArgumentException(message);
  }

  /** Gets a list of C++ source files in the given directory. */
  private File[] listSourceFiles(final String path) {
    if (path == null) return new File[0]; // no sources
    final File sourceDir = new File(path);
    if (!sourceDir.exists()) return new File[0]; // no sources
    return sourceDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(final File pathname) {
        return pathname.getName().toLowerCase().endsWith(".cpp");
      }
    });
  }

  /** Scans the enclosing JAR file for all resources beneath the given path. */
  private List<String> findResources(final String resourceDir)
    throws IOException
  {
    final List<String> resources = new ArrayList<String>();
    final String jarPath = findEnclosingJar(Jar2Lib.class);
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
  private void copyResource(final String resource, final String outPath)
    throws IOException
  {
    log(outPath);
    final File outputFile = new File(outputDir, outPath);
    final File parentDir = outputFile.getParentFile();
    if (!parentDir.exists()) parentDir.mkdirs();
    if (resource.endsWith("/")) {
      // resource is a directory
      outputFile.mkdir();
    }
    else {
      // resource is a file
      final InputStream in = Jar2Lib.class.getResourceAsStream("/" + resource);
      writeStreamToFile(in, outputFile);
      in.close();
    }
  }

  /**
   * Copies the given file to to the source folder
   * of the specified output directory.
   * @throws IOException 
   */
  private void copySourceFile(final File sourceFile) throws IOException {
    log(sourceFile.getPath());
    final File outputFile = new File(outputSourceDir, sourceFile.getName());
    final FileInputStream in = new FileInputStream(sourceFile);
    writeStreamToFile(in, outputFile);
    in.close();
  }

  // -- Static utility methods --

  /**
   * Finds the JAR file (or file system path)
   * from which the given class was loaded.
   */
  public static String findEnclosingJar(final Class<?> c)
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

  /** Locates the JAR file containing this JVM's classes. */
  private static String findRuntime() throws UnsupportedEncodingException {
    return findEnclosingJar(Object.class);
  }

  /** Gets the absolute path to the given file, with forward slashes. */
  private static String path(final File file) {
    final String path = file.getAbsolutePath();
    // NB: Use forward slashes even on Windows.
    return path.replaceAll("\\\\", "/");
  }

  /** Builds a classpath corresponding to the given list of JAR files. */
  private static String classpath(final List<String> libraryJars)
    throws UnsupportedEncodingException
  {
    final StringBuilder sb = new StringBuilder();
    final String jrePath = findRuntime();
    sb.append(jrePath);
    for (String jarPath : libraryJars) {
      sb.append(File.pathSeparator);
      sb.append(jarPath);
    }
    final String classPath = System.getProperty("java.class.path");
    if (classPath != null && !classPath.equals("")) {
      sb.append(File.pathSeparator);
      sb.append(classPath);
    }
    final String bootClassPath = System.getProperty("sun.boot.class.path");
    if (bootClassPath != null && !bootClassPath.equals("")) {
      sb.append(File.pathSeparator);
      sb.append(bootClassPath);
    }
    return sb.toString();
  }

  /** Writes the contents of the given input stream to the specified file. */
  private static void writeStreamToFile(final InputStream in,
    final File outputFile) throws IOException
  {
    final OutputStream out = new FileOutputStream(outputFile);
    final byte[] buf = new byte[512 * 1024]; // 512K buffer
    while (true) {
      int r = in.read(buf);
      if (r <= 0) break; // EOF
      out.write(buf, 0, r);
    }
    out.close();
  }

}
