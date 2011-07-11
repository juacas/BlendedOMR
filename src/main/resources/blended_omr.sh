#!/bin/bash
script_path=`dirname $0`
java -cp $script_path/lib -jar $script_path/${project.build.finalName}.jar ${1+$@}
