#!/bin/bash

if [[ -n "$DB_DUMP_DEBUG" ]]; then
  set -x
fi

set -e

mysql -D "${DB_NAMES}" \
      -h "${DB_SERVER}" \
      -u "${DB_USER}" \
      -p"${DB_PASS}" \
      -e 'DELETE FROM flyway_schema_history WHERE version IS null;'
