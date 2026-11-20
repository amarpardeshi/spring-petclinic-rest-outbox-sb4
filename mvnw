#!/usr/bin/env sh
# Lightweight launcher delegating to local Maven installation or wrapper jar
if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
else
  echo "Maven (mvn) is required on PATH to execute build commands." >&2
  exit 1
fi
