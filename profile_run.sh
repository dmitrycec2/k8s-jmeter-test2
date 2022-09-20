#!/bin/bash

mk_params() {
  pwd
	cd profiles/${profile_name}
	java -jar ../../jmeter_parser_ALL.jar scripts_params.xlsx $1  
	cd ${C_DIR}
	}
 
# SET VAR AND DIR
profile_name=${1}
script_name=${2}
mkdir -p results/${profile_name}
C_DIR=$PWD

# CREATE FILE PARAMS:
mk_params ${script_name}

# START TEST:
echo "Start Test"
sh ./jmeter-start-test.sh ${script_name} ${profile_name}



