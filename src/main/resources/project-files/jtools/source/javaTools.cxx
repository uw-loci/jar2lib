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

using namespace std;

void JavaTools::createJVM()
{
  JavaTools::createJVM("", "", true, 256);
}

void JavaTools::createJVM(string jarlist)
{
  JavaTools::createJVM("", jarlist, true, 256);
}

void JavaTools::createJVM(int memory)
{
  JavaTools::createJVM("", "", true, memory);
}

void JavaTools::createJVM(bool headless)
{
  JavaTools::createJVM("", "", headless, 256);
}

void JavaTools::createJVM(string classdir, int memory)
{
  JavaTools::createJVM(classdir, "", true, memory);
}

void JavaTools::createJVM(string classdir, bool headless)
{
  JavaTools::createJVM(classdir, "", headless, 256);
}

void JavaTools::createJVM(int memory, string jarlist)
{
  JavaTools::createJVM("", jarlist, true, memory);
}

void JavaTools::createJVM(bool headless, string jarlist)
{
  JavaTools::createJVM("", jarlist, headless, 256);
}

void JavaTools::createJVM(string classdir, string jarlist)
{
  JavaTools::createJVM(classdir, jarlist, true, 256);
}

void JavaTools::createJVM(string classdir, string jarlist, int memory)
{
  JavaTools::createJVM(classdir, jarlist, true, memory);
}

void JavaTools::createJVM(string classdir, string jarlist, bool headless)
{
  JavaTools::createJVM(classdir, jarlist, headless, 256);
}

//TODO: Add option override java library path
void JavaTools::createJVM(string classdir, string jarlist, bool headless, int memory)
{

  try {
    jace::StaticVmLoader* tmpLoader = (jace::StaticVmLoader*)jace::helper::getVmLoader();
    if(tmpLoader == NULL) {

      // initialize the Java virtual machine
      jace::OptionList list;
      jace::StaticVmLoader loader(JNI_VERSION_1_4);

      if(classdir.length() >= 1 && classdir.at(classdir.length() - 1) != SLASH ) {
        classdir.append(1,SLASH);
      }

      std::string classpath ("");

      //TODO: Add all Jar2Lib classpath jars to this list by default (by template? by txt file?)
      classpath += classdir + "jace-runtime.jar";

      if(jarlist.length() >= 1)
      {
        classpath += PATHSTEP;

        size_t found;
        found = jarlist.find_first_of(";");
        while(found != string::npos)
        {
          classpath += classdir + jarlist.substr(0, found) + PATHSTEP;

          jarlist = jarlist.substr(found + 1, jarlist.length());

          found = jarlist.find_first_of(";");
        }

        classpath += classdir + jarlist.substr(0, found);
      }

      std::cout << "jarlist : " << jarlist << std::endl;

      std::cout << "Classpath for JVM: " << classpath << std::endl;

      list.push_back(jace::ClassPath(
      classpath
      ));
      list.push_back(jace::CustomOption("-Xcheck:jni"));

      std::string mem;
      std::stringstream out;
      out << memory;
      mem = out.str();

      list.push_back(jace::CustomOption("-Xmx" + mem + "m"));

      if (headless)
        list.push_back(jace::CustomOption("-Djava.awt.headless=true"));
      //list.push_back(jace::CustomOption("-verbose"));
      //list.push_back(jace::CustomOption("-verbose:jni"));
      jace::helper::createVm(loader, list, false);
    }
  }
  catch (jace::JNIException& jniException) {
    std::cerr << "Exception creating JVM: " << jniException.what() << std::endl;
  }
}
