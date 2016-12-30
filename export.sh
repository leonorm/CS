#!/bin/bash

  #ICASA_HOME need to be configured 
  repositoryList=$(ls -d */)
  
  if [ "$1" = "clean" ]
  then 
    rm $ICASA_HOME/applications/*.jar
    for i in $repositoryList
    do 
      jarList=$(ls $i'target'/*.jar)
      echo "suppression de $jarList"
      for j in $jarList
      do 
	rm $j
      done 
    done 
  fi
 
  if [ "$1" = "compileExport" ]
  then 
    for i in $repositoryList
    do 
      jarList=$(ls $i'target'/*.jar)
      echo "MAJ de $jarList"
      for j in $jarList
      do 
	cp $j $ICASA_HOME/applications/
      done 
    done 
  fi
  
  
  if [ "$1" = "compileExportBundlesWithoutServicesInterface" ]
  then 
    echo -e "\nMaj des bundles :"
    repositoryList=$(ls -d */ | grep -E -v api)
    for i in $repositoryList
    do 
      jarList=$(ls $i'target'/*.jar)
      for j in $jarList
      do 
      echo $j
      cp $j $ICASA_HOME/applications/
      done 
    done 
  fi