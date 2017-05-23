#!/usr/bin/env bash
name="$1"
rm ./out/*
nasm -f macho ./out/${name}.asm -o ./out/${name}.o  && gcc -arch i386 -o ./out/${name} ./out/${name}.o
./out/${name}