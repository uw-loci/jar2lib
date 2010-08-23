#!/bin/bash

# NB: Run "mvn assembly:assembly" first to build the complete Jar2Lib JAR file.

JAR2LIB_DIR=`cd "$(dirname $0)/.."; pwd`
REPOSITORY_DIR=~/.m2/repository

java -cp \
"$JAR2LIB_DIR/target/jar2lib-1.0-SNAPSHOT-deps.jar":\
"$REPOSITORY_DIR/imagej/ij/1.43/ij-1.43.jar":\
"$REPOSITORY_DIR/log4j/log4j/1.2.15/log4j-1.2.15.jar" \
  loci.jar2lib.Jar2Lib \
  bfcpp "Bio-Formats C++ bindings" \
  $REPOSITORY_DIR/loci/loci-common/5.0-SNAPSHOT/loci-common-5.0-SNAPSHOT.jar \
  $REPOSITORY_DIR/loci/loci_plugins/5.0-SNAPSHOT/loci_plugins-5.0-SNAPSHOT.jar \
  $REPOSITORY_DIR/loci/ome-xml/5.0-SNAPSHOT/ome-xml-5.0-SNAPSHOT.jar \
  $REPOSITORY_DIR/loci/bio-formats/5.0-SNAPSHOT/bio-formats-5.0-SNAPSHOT.jar \
  $REPOSITORY_DIR/loci/flow-cytometry/1.0-SNAPSHOT/flow-cytometry-1.0-SNAPSHOT.jar \
  -conflicts $JAR2LIB_DIR/bf-cpp/conflicts.txt \
  -header $JAR2LIB_DIR/bf-cpp/header.txt
