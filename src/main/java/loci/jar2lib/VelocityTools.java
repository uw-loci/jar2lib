//
// VelocityTools.java
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Useful methods for working with Apache Velocity.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/jar2lib/src/main/java/loci/jar2lib/VelocityTools.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/jar2lib/src/main/java/loci/jar2lib/VelocityTools.java">SVN</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class VelocityTools {

  public static VelocityEngine createEngine() throws VelocityException {
    // initialize Velocity engine; enable loading of templates as resources
    VelocityEngine ve = new VelocityEngine();
    Properties p = new Properties();
    p.setProperty("resource.loader", "class");
    p.setProperty("class.resource.loader.class",
      "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    try {
      ve.init(p);
    }
    // NB: VelocityEngine.init(Properties) throws Exception.
    catch (Exception exc) {
      throw new VelocityException(exc);
    }
    return ve;
  }

  public static VelocityContext createContext() {
    // populate Velocity context
    VelocityContext context = new VelocityContext();
    context.put("user", System.getProperty("user.name"));
    DateFormat dateFmt = DateFormat.getDateInstance(DateFormat.MEDIUM);
    DateFormat timeFmt = DateFormat.getTimeInstance(DateFormat.LONG);
    Date date = Calendar.getInstance().getTime();
    context.put("timestamp", dateFmt.format(date) + " " + timeFmt.format(date));

    return context;
  }

  public static void processTemplate(VelocityEngine ve,
    VelocityContext context, String inFile, String outFile)
    throws VelocityException, IOException
  {
    System.out.print("Writing " + outFile + ": ");
    final Template t;
    try {
      t = ve.getTemplate(inFile);
    }
    // NB: VelocityEngine.getTemplate(String) throws Exception.
    catch (Exception exc) {
      throw new VelocityException(exc);
    }
    final StringWriter writer = new StringWriter();
    t.merge(context, writer);
    final PrintWriter out = new PrintWriter(new FileWriter(outFile));
    out.print(writer.toString());
    out.close();
    System.out.println("done.");
  }

}
