# simpleio

Purpose - This is a simple tool for evaluating system I/O performance.

Build - Need Maven build tool

mvn clean install package -f simpleio-pom.xml

This command generates simpleio.tgz in the "target" directory.

Use - Need Java 1.8

simpleio.sh -d <target directory> -s <file size> -t <number of threads> -b <benchmark name>

