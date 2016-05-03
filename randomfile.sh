#!/bin/bash

if [ -z "$(java -version 2>&1 | grep 1.8)" ]; then
   echo "Need java runtime version 1.8"
   exit 1
fi

script_dir=`dirname $0`

java -cp ${script_dir}/randomfile-0.0.1-SNAPSHOT.jar:${script_dir}/commons-cli-1.3.1.jar randomfile.DataFileMain $*
