//
// VelocityAutogen.java
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
