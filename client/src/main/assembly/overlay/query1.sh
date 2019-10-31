#!/bin/bash

if [[ $* == *-DoutPath=* ]];
then
  for param in $@
  do
    if [[ $param == -DoutPath=* ]];
    then
      SUBSTRING=$(echo $param| cut -d'=' -f 2)
      if [[ $SUBSTRING == /* ]];
      then
        java $* -cp 'lib/jars/*' "pod.client.Query1Client" > "${SUBSTRING}query1.txt"
      else
        java $* -cp 'lib/jars/*' "pod.client.Query1Client" > "${SUBSTRING}/query1.txt"
      fi
    fi
  done
else
  java $* -cp 'lib/jars/*' "pod.client.Query1Client"
fi

