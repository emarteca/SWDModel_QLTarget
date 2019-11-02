#!/bin/bash

# script to compile simulation files


javac -cp .:jcommon-1.0.18.jar:jfreechart-1.0.14.jar *.java

if [[ ! -d "DATA" ]]; then
	mkdir DATA
fi


