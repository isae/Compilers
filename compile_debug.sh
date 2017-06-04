#!/usr/bin/env bash
name="$1"
WORKING_DIR=$(pwd)
OUT=${WORKING_DIR}/out 
echo "Working directory ${WORKING_DIR}" 
FILENAME=$(basename "$1")
FILENAME="${FILENAME%.*}"
FILENAME=${OUT}/${FILENAME}
gcc -m32 -lc -g ${FILENAME}.expr.S -o ${FILENAME} 
${FILENAME}
