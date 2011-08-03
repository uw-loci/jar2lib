#
# Prerequisites.cmake
#

# CMake build file for cross-platform location of prerequisite libraries,
# including Boost Thread and Java's jni.h.

### search for prerequisite libraries ###

message(STATUS "")

#message("-- Java Runtime:")
#find_package(Java REQUIRED)
#message("java          : ${JAVA_RUNTIME}")
#message("javac         : ${JAVA_COMPILE}")
#message("jar           : ${JAVA_ARCHIVE}")
#message("")

message(STATUS "-- Java Native Interface:")
find_package(JNI REQUIRED)
message(STATUS "jawt lib      : ${JAVA_AWT_LIBRARY}")
message(STATUS "jvm lib       : ${JAVA_JVM_LIBRARY}")
message(STATUS "jni.h         : ${JAVA_INCLUDE_PATH}")
message(STATUS "jni_md.h      : ${JAVA_INCLUDE_PATH2}")
message(STATUS "jawt.h        : ${JAVA_AWT_INCLUDE_PATH}")
message(STATUS "")

# HACK - CMake on Windows refuses to find the thread library unless BOOST_ROOT
#        is set, even though it can locate the Boost directory tree.
#        So we first look for base Boost, then set BOOST_ROOT and look again
#        for Boost Thread specifically.

message(STATUS "-- Boost:")
if(UNIX)
  set(Boost_USE_SHARED_LIBS ON)
endif(UNIX)

if(NOT DEFINED Boost_USE_SHARED_LIBS)
  set(Boost_USE_STATIC_LIBS ON)
endif(NOT DEFINED Boost_USE_SHARED_LIBS)

set(Boost_USE_MULTITHREADED ON)
set(Boost_ADDITIONAL_VERSIONS
  "1.37" "1.37.0" "1.37.1" "1.38" "1.38.0" "1.38.1" "1.39" "1.39.0" "1.39.1"
  "1.40" "1.40.0" "1.40.1" "1.41" "1.41.0" "1.41.1" "1.42" "1.42.0" "1.42.1"
  "1.43" "1.43.0" "1.43.1" "1.44" "1.44.0" "1.44.1" "1.45" "1.45.0" "1.45.1"
  "1.46" "1.46.0" "1.46.1" "1.47" "1.47.0" "1.47.1" "1.48" "1.48.0" "1.48.1"
  "1.49" "1.49.0" "1.49.1" "1.50" "1.50.0" "1.50.1" "1.51" "1.51.0" "1.51.1"
  "1.52" "1.52.0" "1.52.1" "1.53" "1.53.0" "1.53.1" "1.54" "1.54.0" "1.54.1"
  "1.55" "1.55.0" "1.55.1" "1.56" "1.56.0" "1.56.1" "1.57" "1.57.0" "1.57.1"
  "1.58" "1.58.0" "1.58.1" "1.59" "1.59.0" "1.59.1" "1.60" "1.60.0" "1.60.1")
#set(Boost_FIND_QUIETLY ON)
find_package(Boost)
if(IS_DIRECTORY "${Boost_INCLUDE_DIR}")
  message(STATUS "boost headers : ${Boost_INCLUDE_DIR}")
else(IS_DIRECTORY "${Boost_INCLUDE_DIR}")
  if(UNIX)
    message(FATAL_ERROR "Cannot build without Boost Thread library. Please install libboost-thread-dev package or visit www.boost.org.")
  else(UNIX)
    message(FATAL_ERROR "Cannot build without Boost Thread library. Please install Boost from www.boost.org.")
  endif(UNIX)
endif(IS_DIRECTORY "${Boost_INCLUDE_DIR}")
#set(Boost_FIND_QUIETLY OFF)
if(WIN32)
  set(BOOST_ROOT ${Boost_INCLUDE_DIR})
endif(WIN32)
find_package(Boost COMPONENTS thread REQUIRED)

# HACK - Make linking to Boost work on Windows systems.
string(REGEX REPLACE "/[^/]*$" ""
  Boost_STRIPPED_LIB_DIR "${Boost_THREAD_LIBRARY_DEBUG}")

if(EXISTS "${Boost_THREAD_LIBRARY_DEBUG}")
  message(STATUS "boost lib dir : ${Boost_STRIPPED_LIB_DIR}")
  message(STATUS "thread lib    : ${Boost_THREAD_LIBRARY_DEBUG}")
else(EXISTS "${Boost_THREAD_LIBRARY_DEBUG}")
  message(FATAL_ERROR "Cannot build without Boost Thread library. Please install libboost-thread-dev package or visit www.boost.org.")
endif(EXISTS "${Boost_THREAD_LIBRARY_DEBUG}")
message(STATUS "")

# HACK - Make linking to Boost work on Windows systems.
if(WIN32)
  link_directories(${Boost_STRIPPED_LIB_DIR})
endif(WIN32)
