#
# CMakeLists.txt
#

# CMake build file for Jace C++ code.

cmake_minimum_required(VERSION 2.8)

project(jace)

include(jace/Prerequisites.cmake)

### build Jace C++ library ###

include_directories("include"
  "${JAVA_INCLUDE_PATH}" "${JAVA_INCLUDE_PATH2}"
  "${Boost_INCLUDE_DIR}")

file(GLOB_RECURSE JACE_SRC "jace/source/*.cpp")

add_library(jace SHARED ${JACE_SRC})

# NB: Mac OS X 10.4 and earlier requires .jnilib extension rather than
#     .dylib when loading native code with System.loadLibrary(String).
if(APPLE)
  set_target_properties(jace PROPERTIES SUFFIX ".jnilib")
endif(APPLE)

# HACK - Make linking to Boost work on Windows systems.
if(WIN32)
  target_link_libraries(jace "${JAVA_JVM_LIBRARY}")
else(WIN32)
  target_link_libraries(jace
    "${JAVA_JVM_LIBRARY}" ${Boost_THREAD_LIBRARY_DEBUG})
endif(WIN32)

if(CMAKE_COMPILER_IS_GNUCXX)
  set_target_properties(jace PROPERTIES COMPILE_FLAGS "-fpermissive")
else(CMAKE_COMPILER_IS_GNUCXX)
  set_target_properties(jace PROPERTIES DEFINE_SYMBOL JACE_EXPORTS)
endif(CMAKE_COMPILER_IS_GNUCXX)
