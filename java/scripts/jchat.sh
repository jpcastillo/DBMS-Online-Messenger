#!/bin/bash

###
#	CS166 Database Management Systems
#	John Castillo && Daniel Pasillas
#	BASH script for processing input to be fed into JavaChat (jchat)
###

NUM_ARGS=4
ISNUM='^[0-9]+$'
ISALPHA='^[a-zA-Z]+$'
EXITARGS=10
EXITFILE=11
EXITNUM=12
EXITDIR=13
#Messenger (String dbname, String dbport, String user, String passwd)
# Do we have a correct number of arguements?
if [[ $# -ne $NUM_ARGS ]]; then
	echo -e "Error: Invalid number of arguements. Expected four.\
	\nSample usage:\
	\n\t$0 <dname> <dbport> <user> <passwd>";
	exit $EXITARGS;
else

	# Is the seed_file arguement?
	if [[ $1 =~ $ISALPHA ]]; then
		dbname=$1;
	else
		echo "Error: '$1' is not a proper alpha string.";
		exit $EXITFILE;
	fi

	# Is dbport an integer?
	if [[ $2 =~ $ISNUM ]]; then
		dbport=$2;
	else
		echo "Error: Second arguement expected integer got '$2'";
		exit $EXITNUM;
	fi

	# Is user an integer?
	if [[ $3 =~ $ISALPHA ]]; then
		user=$3;
	else
		echo "Error: Third arguement expected alpha string got '$3'";
		exit $EXITNUM;
	fi

	passwd=$4;

fi

##
# Below modified and taken from compile.sh
##

# Current directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";

# Compile
javac -d $DIR/../classes $DIR/../src/Main.java $DIR/../src/Messenger.java;

# Run
java -cp $DIR/../classes:$DIR/../lib/pg73jdbc3.jar Main "$dbname" "$dbport" "$user" "$passwd";

