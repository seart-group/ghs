#!/bin/bash

if [[ -n "$DB_DUMP_DEBUG" ]]; then
  set -x
fi

set -e

find "${DB_DUMP_TARGET}" -mtime +7 -type f -delete
