#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

DATA="$( cd "$DIR"/../../data && pwd)"
psql -p $PGPORT $DB_NAME < $DIR/../src/create_tables.sql
psql -p $PGPORT $DB_NAME < $DIR/../src/create_indexes.sql
psql -p $PGPORT $DB_NAME < $DIR/../src/triggers.sql
cat $DIR/../src/load_data.sql | eval "sed -e 's|<!DATA>|$DATA|g'" | psql -p $PGPORT $DB_NAME
