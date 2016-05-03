# simpleio

Purpose - This is a simple tool for evaluating system I/O performance.

Build - Need Maven build tool

mvn clean install package -f simpleio-pom.xml

This command generates simpleio.tgz in the "target" directory.

Use - Need Java 1.8

To run an I/O benchmark, run the following command

simpleio.sh [-h] -d <target directory> -s <file size> -b <benchmark name> [-t <number of threads>]

To generate a file with random data, run the following command

randomfile.sh [-h] -f <file name> -s <file size> [-l]
