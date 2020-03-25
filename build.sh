#!/bin/bash

rm -fr resources/public/js/*
clj -A:js
cp src/cljs/choices/custom.cljs docs/exemples/clojure.txt
cp resources/public/js/choices.js docs/js/
echo "static js generated"

clj -m choices.generateall
echo "static website generated"
