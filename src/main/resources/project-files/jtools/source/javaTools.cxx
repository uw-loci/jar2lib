//
// javaTools.cxx
//

/*
OME Bio-Formats ITK plugin for calling Bio-Formats from the Insight Toolkit.
Copyright (c) 2008-@year@, UW-Madison LOCI.
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

THIS SOFTWARE IS PROVIDED BY UW-MADISON LOCI ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL UW-MADISON LOCI BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/*
IMPORTANT NOTE: Although this software is distributed according to a
"BSD-style" license, it requires the OME Bio-Formats Java library to do
anything useful, which is licensed under the GPL v2 or later.
As such, if you wish to distribute this software with Bio-Formats itself,
your combined work must be distributed under the terms of the GPL.
*/
#include <iostream>
#include <string>
#include <sstream>
#include "javaTools.h"

#if defined (_WIN32)
#define PATHSTEP ';'
#define SLASH '\\'
#else
#define PATHSTEP ':'
#define SLASH '/'
#endif

#ifdef __APPLE__
#define PATH "DYLD_LIBRARY_PATH"
#endif
#ifdef __linux__
#define PATH "LD_LIBRARY_PATH"
#endif
#ifdef _WIN32
#define PATH "PATH"
#endif
#ifdef _WIN64
#define PATH "PATH"
#endif

using namespace std;

void JavaTools::createJVM()
{
  JavaTools::createJVM("jar", true, 256, vector<string>(), vector<string>(), vector<string>());
}

void JavaTools::createJVM(string jarFolder)
{
  JavaTools::createJVM(jarFolder, true, 256, vector<string>(), vector<string>(), vector<string>());
}

void JavaTools::createJVM(int memory)
{
  JavaTools::createJVM("jar", true, memory, vector<string>(), vector<string>(), vector<string>());
}

void JavaTools::createJVM(bool headless)
{
  JavaTools::createJVM("jar", headless, 256, vector<string>(), vector<string>(), vector<string>());
}

void JavaTools::createJVM(bool headless, int memory)
{
  JavaTools::createJVM("jar", headless, memory, vector<string>(), vector<string>(), vector<string>());
}

void JavaTools::createJVM(string jarFolder, bool headless, int memory)
{
  JavaTools::createJVM(jarFolder, headless, memory, vector<string>(), vector<string>(), vector<string>());
}

/**
 * jarFolder is the name of the path to a directory of jars to be added to the classpath (default: jar)
 * headless turns headless on or off for JVM instantiation (default: on)
 * memory controls memory for instantiation of JVM in MB (default: 256)
 * extraClasspath is a list of additional classpath entries to include
 * extraJavaLibraryPath is a list of path entries to be appended to the JVM's java.library.path. ( default: .)
 * extraOptions is a list of any other desired arguments to the JVM, added to the jace::OptionList (e.g., "-verbose:jni")
 */
void JavaTools::createJVM(string jarFolder, bool headless, int memory,
  vector<string> extraClasspath, vector<string> extraJavaLibraryPath, vector<string> extraOptions)
{
  try {
    jace::StaticVmLoader* tmpLoader = (jace::StaticVmLoader*)jace::helper::getVmLoader();
    if(tmpLoader == NULL) {

      // initialize the Java virtual machine
      jace::OptionList list;
      jace::StaticVmLoader loader(JNI_VERSION_1_4);

      string classpath ("");

      DIR *d;
      struct dirent *dir;

//TODO: test jar/*
      // All .jar files in the jarFolder are added to the JVM's classpath
      d = opendir(jarFolder.c_str());

      if(d)
      {
        while ((dir = readdir(d)) != NULL)
        {
          string tmpName(dir->d_name);

          if(tmpName.find(".jar") != string::npos)
          {
            if(classpath.length() >= 1)
            {
              classpath += PATHSTEP;
            }
            classpath += jarFolder + SLASH + dir->d_name;
          }
        }

        closedir(d);
      }

      // All items in the extraClasspath are added to the JVM's classpath
      if(!extraClasspath.empty())
      {
        for(vector<string>::iterator it = extraClasspath.begin(); it != extraClasspath.end(); it++)
        {
          classpath += PATHSTEP;
          classpath += *it;
        }
      }

      //std::cout << "extraClasspath : " << extraClasspath << std::endl;
      //std::cout << "Classpath for JVM: " << classpath << std::endl;

      // Gets the system-specific path environment variable's contents (which will eventually be passed to java.library.path in this JVM)
      std::string pName = PATH;
      char * libPath;
      libPath = getenv(pName.c_str());
      std::string jLibPath("");
      if(libPath != NULL)
      {
        jLibPath = libPath;
      }

      // If no extra Java library path is specified, the current working directory is appended and passed to java.library.path.
      // Otherwise, each provided path is appended.
      if(extraJavaLibraryPath.empty())
      {
        if(jLibPath.length() >= 1)
        {
          jLibPath += PATHSTEP;
        }
        jLibPath += '.';
      }
      else
      {
        for(vector<string>::iterator it = extraJavaLibraryPath.begin(); it != extraJavaLibraryPath.end(); it++)
        {
          if(jLibPath.length() >= 1)
          {
           jLibPath += PATHSTEP;
          }
          jLibPath += *it;
        }
      }

      list.push_back(jace::ClassPath(classpath));
      list.push_back(jace::CustomOption("-Xcheck:jni"));

      // Int to string conversion
      std::string mem;
      std::stringstream out;
      out << memory;
      mem = out.str();

      list.push_back(jace::CustomOption("-Xmx" + mem + "m"));

      if (headless)
      {
        list.push_back(jace::CustomOption("-Djava.awt.headless=true"));
      }

      list.push_back(jace::CustomOption("-Djava.library.path=" + jLibPath));

      // All extra options are added as CustomOptions for JVM instantiation
      if (!extraOptions.empty()) {
        for (vector<string>::iterator it = extraOptions.begin(); it != extraOptions.end(); it++)
        {
          list.push_back(jace::CustomOption(*it));
        }
      }

      jace::helper::createVm(loader, list, false);
    }
  }
  catch (jace::JNIException& jniException) {
    std::cerr << "Exception creating JVM: " << jniException.what() << std::endl;
  }
}
