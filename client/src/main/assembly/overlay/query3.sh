#!/bin/bash

if [[ $* == *-DoutPath=* ]];
then
  for param in $@
  do
    if [[ $param == -DoutPath=* ]];
    then
      SUBSTRING=$(echo $param| cut -d'=' -f 2)
      java $* -cp 'lib/jars/*' "pod.client.Query3Client" > "${SUBSTRING}query3.txt"
    fi
  done
else
  java $* -cp 'lib/jars/*' "pod.client.Query3Client"
fi

