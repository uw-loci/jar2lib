> NB: This project has been discontinued.
> It remains archived here for historical reasons.
> For a discussion of other integration solutions, see
> [Interfacing From Non-Java Code](https://uw-loci.github.io/interfacing-non-java-code).

# Jar2Lib

Jar2Lib is a command line tool for generating C++ wrapper libraries
around Java JAR files. It exposes the entire public Java API of the JAR
in the corresponding C++ shared library, delegating to Java via JNI. The
wrapping is performed using [Jace](https://github.com/cowwoc/jace/).

We use Jar2Lib to generate the
[BF-CPP](https://github.com/ome/bio-formats-jace)
bindings for [Bio-Formats](https://loci.wisc.edu/software/bio-formats),
which we use in
our [WiscScan](https://loci.wisc.edu/software/wiscscan) acquisition
software.

*Historical note: Jar2Lib was originally developed to integrate
Bio-Formats with the [Insight Toolkit](https://itk.org/), for use with
the FARSIGHT project. However, we now have a [dedicated inter-process
solution for ITK integration](https://github.com/scifio/scifio-imageio)
instead which is easier to build from source, since it has no
compile-time dependencies.*

## Download

- [jace-r39.jar](https://maven.scijava.org/content/repositories/thirdparty/jace/jace/r39/jace-r39.jar)
- [jar2lib-1.1.1.jar](https://maven.scijava.org/content/repositories/releases/loci/jar2lib/1.1.1/jar2lib-1.1.1.jar)

## Instructions

We recommend using Jar2Lib via the cppwrap-maven-plugin; see
"Installation and usage" below.

If you prefer to use Jar2Lib directly from the command line outside
Maven:

1. Download the JARs linked above, as well as [Apache Velocity
   1.7](https://search.maven.org/#artifactdetails%7Corg.apache.velocity%7Cvelocity%7C1.7%7Cjar)
   and its dependencies.
2. With all necessary JARs in your current working directory, you can
   run Jar2Lib on the command line as follows:

```
$ java -cp '*' loci.jar2lib.Jar2Lib
Usage: java loci.jar2lib.Jar2Lib projectId projectName
           library.jar [library2.jar ...]
           [-conflicts conflicts.txt] [-header header.txt]
           [-extras cmake_extras.txt] [-output /path/to/output-project]
           [-core java_core_classes.txt `
```

## Installation and Usage

The easiest way to use Jar2Lib is via our [cppwrap Maven
plugin](https://github.com/uw-loci/cppwrap-maven-plugin), which takes
advantage of the [Maven](https://maven.apache.org/) plug-in
infrastructure to easily convert a Java library to C++ using Jar2Lib,
without requiring any knowledge of Jar2Lib's workings or syntax. For
example, it circumvents any need to download transitive dependencies.
