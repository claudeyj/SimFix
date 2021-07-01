#!/bin/sh  
# Define some constants  
PROJECT_PATH=.
JAR_PATH=$PROJECT_PATH/lib  
TARGET_PATH=$PROJECT_PATH/target
SRC_PATH=$PROJECT_PATH/src
CLASSPATH=$CLASSPATH:$JRE_HOME/lib:$PROJECT_PATH/lib/*
  
# First remove the sources.list file if it exists and then create the sources file of the project  
rm -f $SRC_PATH/sources
find $SRC_PATH -name *.java > $SRC_PATH/sources.list
# find $JAR_PATH -name *.jar > $JAR_PATH/jars.list 

# First remove the ONSServer directory if it exists and then create the bin directory of ONSServer  
rm -rf $TARGET_PATH
mkdir $TARGET_PATH  

# Compile the project  
javac -XDignore.symbol.file=true -d $TARGET_PATH -classpath $CLASSPATH -sourcepath src @$SRC_PATH/sources.list

# this is a test
defects4j checkout -p Chart -v 1b -w /data/lambda/d4jrepos/chart/chart_1_buggy
java -cp $TARGET_PATH:lib/* cofix.main.Main --proj_home=/data/lambda/d4jrepos --proj_name=chart --bug_id=1 -ea