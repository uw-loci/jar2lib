JAR2LIB_DIR=`cd "$(dirname $0)/.." && pwd`
ARTIFACT_DIR=~/code/LOCI/java/artifacts
java -cp \
  "$JAR2LIB_DIR/target/"'*'":$JAR2LIB_DIR/target/dependency/"'*' \
  loci.jar2lib.Jar2Lib bfcpp "Bio-Formats C++ bindings" \
  $ARTIFACT_DIR/loci-common.jar \
  $ARTIFACT_DIR/ome-xml.jar \
  $ARTIFACT_DIR/bio-formats.jar \
  $ARTIFACT_DIR/flow-cytometry.jar \
  -conflicts $JAR2LIB_DIR/bf-cpp/conflicts.txt \
  -header $JAR2LIB_DIR/bf-cpp/header.txt
