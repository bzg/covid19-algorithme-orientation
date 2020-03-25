#!/bin/bash

rm -fr resources/public/js/*
clj -A:js
cp resources/public/js/choices.js docs/js/
echo "static js generated"

clj -m choices.generateall
echo "static website generated"
