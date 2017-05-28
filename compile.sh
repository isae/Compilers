#!/usr/bin/env bash
name="$1"
WORKING_DIR=$(pwd)
OUT=${WORKING_DIR}/out 
echo "Working directory ${WORKING_DIR}" 
FILENAME=$(basename "$1")
FILENAME="${FILENAME%.*}"
FILENAME=${OUT}/${FILENAME}
nasm -f macho -g ${OUT}/${name}.asm -o ${FILENAME}.o  && gcc -arch i386 -m32 -g -O0 -o ${FILENAME} ${FILENAME}.o
${FILENAME}