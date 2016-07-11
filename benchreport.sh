#!/bin/bash

if [ -z "$(java -version 2>&1 | grep 1.8)" ]; then
   echo "Need java runtime version 1.8"
   exit 1
fi

script_dir=`dirname $0`

jars="${script_dir}/benchreport-0.0.1-SNAPSHOT.jar:\
${script_dir}/google-api-client-1.22.0.jar:\
${script_dir}/google-oauth-client-1.22.0.jar:\
${script_dir}/google-http-client-1.22.0.jar:\
${script_dir}/jsr305-1.3.9.jar:\
${script_dir}/httpclient-4.0.1.jar:\
${script_dir}/httpcore-4.0.1.jar:\
${script_dir}/commons-logging-1.1.1.jar:\
${script_dir}/commons-codec-1.3.jar:\
${script_dir}/google-http-client-jackson2-1.22.0.jar:\
${script_dir}/jackson-core-2.1.3.jar:\
${script_dir}/guava-jdk5-17.0.jar:\
${script_dir}/google-api-services-sheets-v4-rev8-1.22.0.jar:\
${script_dir}/google-oauth-client-java6-1.22.0.jar:\
${script_dir}/google-oauth-client-jetty-1.22.0.jar:\
${script_dir}/jetty-6.1.26.jar:\
${script_dir}/jetty-util-6.1.26.jar:\
${script_dir}/servlet-api-2.5-20081211.jar"

java -cp ${jars} benchreport.BenchReport $*

