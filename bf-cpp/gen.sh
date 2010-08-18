#!/bin/bash

# NB: Run "mvn assembly:assembly" first to build the complete Jar2Lib JAR file.

JAR2LIB_DIR=`cd "$(dirname $0)/.." && pwd`
ARTIFACT_DIR=~/code/LOCI/java/artifacts
java -jar "$JAR2LIB_DIR/target/jar2lib-1.0-SNAPSHOT-deps.jar" \
  bfcpp "Bio-Formats C++ bindings" \
  $ARTIFACT_DIR/loci-common.jar \
  $ARTIFACT_DIR/ome-xml.jar \
  $ARTIFACT_DIR/bio-formats.jar \
  $ARTIFACT_DIR/flow-cytometry.jar \
  -conflicts $JAR2LIB_DIR/bf-cpp/conflicts.txt \
  -header $JAR2LIB_DIR/bf-cpp/header.txt
