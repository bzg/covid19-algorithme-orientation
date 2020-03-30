#!/bin/bash

# Compile js for dev.cljs.edn
if [[ $1 == "dev" ]]; then
   rm -fr resources/public/js/*
   clj -A:js
   cp resources/public/js/choices.js docs/js/
   echo "static js generated"
fi

# Compile js only for dev-nav.
if [[ $1 == "nav" ]]; then
   rm -fr resources/public/js/*
   clj -A:js-nav
   cp resources/public/js/choices-nav.js docs/js/
   echo "static js generated"
fi

# Update implementation example
cp src/cljs/choices/custom.cljs docs/exemples/clojure.txt

# Build json files
clj -m choices.json

# Build *.html
clj -m choices.build
