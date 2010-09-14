//
// VelocityAutogen.java
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Automatically generates code for a Jar2Lib project. Specifically:
 * <ol>
 *   <li>C++ header files for use with Jace, listing all
 *     classes within the given JAR files.</li>
 *   <li>A CMake build file for use compiling the project.</li>
 * </ol>
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/jar2lib/src/main/java/loci/jar2lib/VelocityAutogen.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/jar2lib/src/main/java/loci/jar2lib/VelocityAutogen.java">SVN</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class VelocityAutogen {

  // -- Fields --

  private String javaHeader, scriptHeader;

  // -- Constructor --

  public VelocityAutogen(String headerPath) throws IOException {
    if (headerPath == null) javaHeader = scriptHeader = "";
    else {
      final File headerFile = new File(headerPath);
      if (!headerFile.exists()) {
        throw new IllegalArgumentException("Invalid header file: " +
          headerPath);
      }
      final BufferedReader in = new BufferedReader(new FileReader(headerFile));
      final StringBuilder javaBuilder = new StringBuilder("\n");
      final StringBuilder scriptBuilder = new StringBuilder("\n");
      while (true) {
        final String line = in.readLine();
        if (line == null) break; // EOF
        javaBuilder.append("//");
        if (!line.equals("")) javaBuilder.append(" ");
        javaBuilder.append(line);
        javaBuilder.append("\n");
        scriptBuilder.append("#");
        if (!line.equals("")) scriptBuilder.append(" ");
        scriptBuilder.append(line);
        scriptBuilder.append("\n");
      }
      in.close();
      javaHeader = javaBuilder.toString();
      scriptHeader = scriptBuilder.toString();
    }
  }

  // -- VelocityAutogen methods --

  public void createJaceHeader(String jarPath, String outputPath)
    throws VelocityException, IOException
  {
    final String jarName = new File(jarPath).getName();
    int dot = jarName.lastIndexOf(".");
    if (dot < 0) dot = jarName.length();
    final String headerName = jarName.substring(0, dot) + ".h";
    final String headerLabel = headerName.toUpperCase().replaceAll("\\W", "_");
    final File headerFile = new File(outputPath, headerName);
    final String headerPath = headerFile.getAbsolutePath();

    // parse header file template
    final ClassList classList = new ClassList(jarPath);

    // initialize Velocity
    VelocityEngine ve = VelocityTools.createEngine();
    VelocityContext context = VelocityTools.createContext();

    context.put("headerBlock", javaHeader);
    context.put("headerLabel", headerLabel);
    context.put("headerName", headerName);
    context.put("q", classList);

    // generate C++ header file
    VelocityTools.processTemplate(ve, context, "jace-header.vm", headerPath);
  }

  public void createCMakeLists(String projectId, String projectName,
    String outputPath) throws VelocityException, IOException
  {
    final File buildFile = new File(outputPath, "CMakeLists.txt");
    final String buildPath = buildFile.getAbsolutePath();

    // initialize Velocity
    VelocityEngine ve = VelocityTools.createEngine();
    VelocityContext context = VelocityTools.createContext();

    context.put("headerBlock", scriptHeader);
    context.put("projectId", projectId);
    context.put("projectName", projectName);

    // generate CMakeLists.txt file
    VelocityTools.processTemplate(ve, context, "CMakeLists.vm", buildPath);
  }

}
