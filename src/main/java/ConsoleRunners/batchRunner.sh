#!/bin/bash

# batch running script

# args[0] -- population number
# args[1] -- stage (1 -> eggs, 2 -> females)
# args[2] -- day to add the flies

javac -cp .:jcommon-1.0.18.jar:jfreechart-1.0.14.jar *.java

popVals=`echo '10 100 1000 10000'`

for val in $popVals; do
	stage=1
	while (( $stage < 3 )); do
		addDate=0
		while (( $addDate < 365 )); do
			java -cp .:jcommon-1.0.18.jar:jfreechart-1.0.14.jar Runner $val $stage $addDate
			#echo "$val : $stage : $addDate"
			(( addDate = $addDate + 1 ))
		done
		(( stage = $stage + 1 ))
	done
done
	
