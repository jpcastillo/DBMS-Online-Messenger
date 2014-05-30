#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
psql81 -p $PGPORT $DB_NAME < $DIR/../src/create_tables.sql
psql81 -p $PGPORT $DB_NAME < $DIR/../src/create_indexes.sql
psql81 -p $PGPORT $DB_NAME < $DIR/../src/load_data.sql
