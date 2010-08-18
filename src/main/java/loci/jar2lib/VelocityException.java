//
// VelocityException.java
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

/**
 * VelocityException is the exception thrown when something
 * goes wrong with an Apache Velocity operation.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/jar2lib/src/main/java/loci/jar2lib/VelocityException.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/jar2lib/src/main/java/loci/jar2lib/VelocityException.java">SVN</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class VelocityException extends Exception {

  public VelocityException() { super(); }
  public VelocityException(String s) { super(s); }
  public VelocityException(String s, Throwable cause) { super(s, cause); }
  public VelocityException(Throwable cause) { super(cause); }

}
