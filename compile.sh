#!/usr/bin/env bash
name="$1"
WORKING_DIR=$(pwd)
OUT=${WORKING_DIR}/out 
echo "Working directory ${WORKING_DIR}" 
FILENAME=$(basename "$1")
FILENAME="${FILENAME%.*}"
FILENAME=${OUT}/${FILENAME}
nasm -f elf32 -F dwarf -g ${OUT}/${name}.asm -o ${FILENAME}.o  && gcc -m32 -lc -g -e -o ${FILENAME} ${FILENAME}.o -Wl,-e,main 
${FILENAME}
