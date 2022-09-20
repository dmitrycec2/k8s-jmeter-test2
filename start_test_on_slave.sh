#!/usr/bin/env bash
#Script created to launch Jmeter tests directly from the current terminal without accessing the jmeter master pod.
#It requires that you supply the path to the jmx file
#After execution, test script jmx file may be deleted from the pod itself but not locally.

working_dir="`pwd`"

#Get namesapce variable
tenant=`awk '{print $NF}' "$working_dir/tenant_export"`

jmx="$1"
profile="$3"
[ -n "$jmx" ] || read -p 'Enter path to the jmx file ' jmx

if [ ! -f "$jmx" ];
then
    echo "Test script file was not found in PATH"
    echo "Kindly check and input the correct file path"
    exit
fi


test_name=$(basename ${jmx%.jmx})
data_path=${jmx%.jmx}
echo "data_path ${data_path}"
#test_name="${test_name,,}"
echo "$test_name"
echo "$test_name"| tr '[:upper:]' '[:lower:]'
echo "$(basename ${jmx%.jmx})"

product_name=$(basename ${working_dir})
echo "product_name ${product_name}"

IFS="," read -a slavearray <<< $2
echo "My array: ${slavearray[@]}"
echo "Number of elements in the array: ${#slavearray[@]}"
jmeter=$(eval "kubectl exec -n alrosa --stdin jmeter-0 -- bash -c "pwd| awk -F'/' '{print $3}'"")
echo "jmeter "$jmeter

# for array iteration
slavenum=${#slavearray[@]}
echo "slavesnum  "$slavenum
# for split command suffix and seq generator
slavedigit="${#slavenum}"
echo "slavedigit  "$slavedigit
printf "Number of slaves is %s\n" "${slavesnum}"



remote_hosts=""

for ((i=0; i<${slavenum}; i++))
do
echo "1"${profile}
    #echo "Copying scenario/${test_name} to ${slavearray[$i]}"
kubectl exec -n $tenant --stdin "${slavearray[i]}" -- bash -c "mkdir -p /opt/$jmeter/bin/${product_name}"
kubectl exec -n $tenant --stdin "${slavearray[i]}" -- bash -c "mkdir -p /opt/$jmeter/bin/${product_name}/scripts"
kubectl exec -n $tenant --stdin "${slavearray[i]}" -- bash -c "mkdir -p /opt/$jmeter/bin/${product_name}/profiles"
kubectl exec -n $tenant --stdin "${slavearray[i]}" -- bash -c "mkdir -p /opt/$jmeter/bin/${product_name}/profiles/${profile}"
echo "2"${profile}
kubectl cp "$jmx" -n $tenant "${slavearray[i]}":/opt/$jmeter/bin/${product_name}/scripts/$test_name.jmx
kubectl cp "profile_run.sh" -n $tenant "${slavearray[i]}":/opt/$jmeter/bin/${product_name}/profile_run.sh
kubectl exec -n $tenant --stdin "${slavearray[i]}" -- bash -c "chmod 777 /opt/$jmeter/bin/${product_name}/profile_run.sh"
kubectl cp "jmeter-start-test.sh" -n $tenant "${slavearray[i]}":/opt/$jmeter/bin/${product_name}/jmeter-start-test.sh
kubectl exec -n $tenant --stdin "${slavearray[i]}" -- bash -c "chmod 777 /opt/$jmeter/bin/${product_name}/jmeter-start-test.sh"
kubectl cp "jmeter_parser_ALL.jar" -n $tenant "${slavearray[i]}":/opt/$jmeter/bin/${product_name}/jmeter_parser_ALL.jar
echo "3"
kubectl cp "profiles/${profile}/scripts_params.xlsx" -n $tenant "${slavearray[i]}":/opt/$jmeter/bin/${product_name}/profiles/${profile}/scripts_params.xlsx
echo "4" ${profile}
pwd
remote_hosts=$remote_hosts${slavearray[i]}".jmeter:1099"
if [[ i -ne ${slavenum}-1 ]];
	then
	remote_hosts=$remote_hosts","
	fi
echo "-------------------------------------------------"
echo $remote_hosts
done # for i in "${slave_pods[@]}"




# Split and upload csv files
printf "data_path is %s\n" "${data_path}"
for csvfilefull in "${data_path}"/*.csv
  do
  csvfile="${csvfilefull##*/}"
  printf "Processing %s file..\n" "$csvfile"
  split --suffix-length="${slavedigit}" --additional-suffix=.csv -d --number="l/${slavenum}" "${data_path}/${csvfile}" "$data_path"/
  
    j=0
  for i in $(seq -f "%0${slavedigit}g" 0 $((slavenum-1)))
    do


      echo $data_path         
      printf "Copy %s to %s on %s\n" "${j}.csv" "${csvfile}" "${slavearray[j]}"
      kubectl -n "$tenant" exec "${slavearray[j]}" -- mkdir -p "${product_name}/${data_path}"
      kubectl -n "$tenant" cp "${data_path}/${i}.csv" "${slavearray[j]}":/opt/$jmeter/bin/${product_name}/${data_path}/${csvfile}
      
      #rm -v "${data_path}/${i}.csv"
  
      let j=j+1
    done # for i in "${slave_pods[@]}"
 
 #linestotal=$(cat "${csvfilefull}" | wc -l)
 #echo "linestotal "$linestotal
 #echo "slavenuml "$slavenum
 #echo "linestotal/slavenum "$((linestotal/slavenum))
 #split --suffix-length="${slavedigit}" --additional-suffix=.csv -d -l $((linestotal/slavenum)) "${data_path}/${csvfile}" "$data_path"/



done # for csvfile in "${jmx_dir}/*.csv"







for ((i=0; i<${slavenum}; i++))
do
pwd
ls
kubectl exec -ti -n $tenant ${slavearray[i]} -- bash -c "pwd"
kubectl exec -ti -n $tenant ${slavearray[i]} -- bash -c "cd /opt/$jmeter/bin/${product_name}"
kubectl exec -ti -n $tenant ${slavearray[i]} -- bash -c "pwd"
kubectl exec -ti -n $tenant ${slavearray[i]} -- bash -c "cd /opt/$jmeter/bin/${product_name};pwd;./profile_run.sh ${profile} $test_name"
#kubectl exec -n $tenant --stdin --tty "${slavearray[i]}" -- bash -c "chmod 777 /opt/$jmeter/bin/${product_name}/jmeter-start-test.sh"

#kubectl exec -ti -n $tenant ${slavearray[i]} -- bash ./jmeter -n -t $test_name.jmx -l /host/jmeter_results/${test_name}_${slavearray[i]}.jtl
kubectl cp -n $tenant "${slavearray[i]}":/opt/$jmeter/bin/${product_name}/results/${profile}/${test_name}_${slavearray[i]}.jtl "$working_dir"/jmeter_results2/${test_name}_${slavearray[i]}.jtl
echo "--------executed  "${slavearray[i]}
pwd


done 