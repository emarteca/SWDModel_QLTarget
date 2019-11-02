#!/bin/bash

# batch running script

# args[0] -- population number (10, 100, 1000, 10000)
# args[1] -- stage (1 -> eggs, 2 -> females) 
# args[2] -- day to add the flies (between 0 and 364 inclusive)

# NOTE: total population is on line 371

popVals=`echo '10.0 100.0 1000.0 10000.0'`

stage=$1

lifeStage=eggs

if (( $stage == 2 )); then
	lifeStage=females
fi

outputFile=`echo 'PROCESSED/processed'$lifeStage'.txt'`

echo 'Starting value--:10:100:1000:10000' > $outputFile
echo '-----------:-----------:-----------:-----------:-----------:' >> $outputFile
echo 'Day:' >> $outputFile

addDate=0
while (( $addDate < 365 )); do
	out=$addDate'--'
	for val in $popVals; do
		fileName=`echo 'output__'$val$lifeStage'_addedDay'$addDate'_365.0daysRun.txt'`
		line=`sed -n '371p' $fileName`
		tot=`echo $line | awk '{
					total = 0
					i = 1
					while (i <= NF) {
						total = total + $i
						i = i + 1
					}
					print total

			
				}'`
		out=$out':'$tot
	done
	echo $out >> $outputFile
	(( addDate = $addDate + 1 ))
			
done


