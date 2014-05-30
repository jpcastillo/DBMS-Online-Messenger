#!/bin/bash

psql81 -p $PGPORT $DB_NAME < drop_indexes.sql > /dev/null
sleep 1

echo "Query time without indexes"
cat <(echo '\timing') queries.sql |psql81 -p $PGPORT $DB_NAME | grep Time | awk -F "Time" '{print "Query" FNR $2;}'

psql81 -p $PGPORT $DB_NAME < create_indexes.sql > /dev/null

echo "Query time with indexes"
cat <(echo '\timing') queries.sql |psql81 -p $PGPORT $DB_NAME | grep Time | awk -F "Time" '{print "Query" FNR $2;}'

