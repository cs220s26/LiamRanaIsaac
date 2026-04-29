#!/bin/bash

if command -v redis-cli >/dev/null 2>&1; then
  REDIS_CLI=redis-cli
elif command -v redis6-cli >/dev/null 2>&1; then
  REDIS_CLI=redis6-cli
else
  echo "Error: Neither redis-cli nor redis6-cli found in PATH." >&2
  exit 1
fi

if ! "$REDIS_CLI" ping >/dev/null 2>&1; then
  echo "Error: Redis is not running or not reachable on port 6379." >&2
  exit 1
fi

"$REDIS_CLI" FLUSHALL
