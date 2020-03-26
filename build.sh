#!/bin/bash

if [[ $1 == "all" ]]; then
   rm -fr resources/public/js/*
   clj -A:js
   cp src/cljs/choices/custom.cljs docs/exemples/clojure.txt
   cp resources/public/js/choices.js docs/js/
   echo "static js generated"
fi

# Build json files
clj -m choices.json

# Build *.html
clj -m choices.build

