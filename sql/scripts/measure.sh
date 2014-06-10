#!/bin/bash

psql -p $PGPORT $DB_NAME < ../src/drop_indexes.sql > /dev/null
sleep 1

echo "Query time without indexes"
cat <(echo '\timing') ../src/queries.sql |psql -p $PGPORT $DB_NAME | grep Time | awk -F "Time" '{print "Query" FNR $2;}'

psql -p $PGPORT $DB_NAME < ../src/create_indexes.sql > /dev/null

echo "Query time with indexes"
cat <(echo '\timing') ../src/queries.sql |psql -p $PGPORT $DB_NAME | grep Time | awk -F "Time" '{print "Query" FNR $2;}'

