//
// javaTools.h
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

#include <string>
#include <vector>
#include <iostream>
#include "jace.h"
#include "jace/JNIHelper.h"
#include "jace/StaticVmLoader.h"
#include "jace/JNIException.h"
#include "jace/OptionList.h"

#ifdef WIN32
  #ifndef MYAPI_API
    #define MYAPI_API __declspec(dllexport)
  #endif
#else
  #ifndef MYAPI_API
    #define MYAPI_API
  #endif
#endif

using namespace std;

/**
 * JavaTools provides static helper methods used in more than one
 * of the various BFITK plugin implementations.
 */
class MYAPI_API JavaTools
{
  public:
    typedef JavaTools      Self;

    /**
     * This method is used to instantiate a JVM with the following defaults:
     * jarFolder: jar
     * memory: 256MB
     * headless: true
     * extraJavaLibraryPath: current working directory
     * extraClasspath: none
     */
    static void createJVM();

    /**
     * This method is used to instantiate a JVM with a custom class path.
     */
    static void createJVM(string jarFolder);

    /**
     * This method is used to instantiate a JVM with custom memory specifications.
     */
    static void createJVM(bool headless);

    /**
     * This method is used to instantiate a JVM with specified headless mode.
     */
    static void createJVM(int memory);

    /**
     * This method is used to instantiate a JVM with a custom class path and
     * memory specifications..
     */
    static void createJVM(bool headless, int memory);

    /**
     * This method is used to instantiate a JVM with a custom class path and
     * specified headless mode.
     */
    static void createJVM(string jarFolder, bool headless, int memory);

    /**
     * This method is the fundamental JVM creation method.
     * classdir - specifies location of the core jar files, and extra jars (if present)
     * jarlist - a ; separated list of additional jars to include in the JVM classpath.
     *           jace-runtime.jar, loci_tools.jar and bio-formats.jar are always included.
     * headless - whether to run headless or not
     * memory - how much memory to instantiate the JVM with
     */
    static void createJVM(string jarFolder, bool headless, int memory, vector<string> extraClasspath, vector<string> extraJavaLibraryPath, vector<string> extraOptions);
};
