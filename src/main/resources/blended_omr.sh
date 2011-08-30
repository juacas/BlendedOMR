#!/bin/bash
#script_path=`dirname $0`
#script_path=/home/idelab/workspace/PFC_OpticalMarkRecognition/target/install/OpticalMarkRecognition
script_path=/usr/local/src/OpticalMarkRecognition/target/install/OpticalMarkRecognition
java -cp $script_path/lib -jar $script_path/${project.build.finalName}.jar "$@"
