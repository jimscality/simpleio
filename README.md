# simpleio

Purpose - This is a simple tool for evaluating system I/O performance.

How To Build

Need Maven build tool

mvn clean install package -f simpleio-pom.xml

This command generates simpleio.tgz in the "target" directory.

How To Use

Need Java 1.8

Run the following command

simpleio.sh [-h] -d <target directory> -s <file size> -b <benchmark name> [-t <number of threads>]

To read or create a file with random data, run the following command

randomfile.sh [-h] -p [read|write] -f <file name> [-s <file size> [-l]]
