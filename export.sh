#!/bin/bash

  #ICASA_HOME need to be configured 
  repositoryList=$(ls -d */)
  
  if [ "$1" = "clean" ]
  then 
    rm $ICASA_HOME/applications/*.jar
  fi
 
  if [ "$1" = "compileExport" ]
  then 
    for i in $repositoryList
    do 
      jarList=$(ls $i'target'/*.jar)
      echo $jarList
      for j in $jarList
      do 
	cp $j $ICASA_HOME/applications/
      done 
    done 
  fi
  