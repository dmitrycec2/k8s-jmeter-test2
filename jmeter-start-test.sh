#!/bin/bash

run_script() {
	sh ${JMETER_BIN}/jmeter -n -p profiles/${profile_name}/$1.properties -t ${T_DIR}/$1.jmx -l ${R_DIR}/$1.jtl -j ${R_DIR}/jmeter.log
	#echo profiles/${profile_name}/$1.properties -t ${T_DIR}/$1.jmx -l ${R_DIR}/$1.jtl -j ${R_DIR}/jmeter.log
	}
 
# SET VAR AND DIR

JMETER_VERSION="5.4" 
JMETER_HOME=/opt/apache-jmeter-${JMETER_VERSION}
JMETER_BIN=${JMETER_HOME}/bin
profile_name=${2}
T_DIR=scripts
R_DIR=results/${profile_name}
C_DIR=$PWD

#RUN SCRIPT:
run_script ${1}




