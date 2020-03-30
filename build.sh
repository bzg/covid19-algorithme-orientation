#!/bin/bash

# Update implementation example
cp src/cljs/choices/custom.cljs docs/exemples/clojure.txt

compile_upload_js () {
    rm -fr resources/public/js/*
    clj -A:js
    cp resources/public/js/choices.js docs/js/
    echo "static js generated"
}

compile_upload_js_nav () {
    rm -fr resources/public/js/*
    clj -A:js-nav
    cp resources/public/js/choices-nav.js docs/js/
    echo "static js generated for nav"
}

# Compile js for dev.cljs.edn
if [[ $1 == "js" ]]; then
    compile_upload_js
fi

# Compile js only for dev-nav.
if [[ $1 == "js-nav" ]]; then
    compile_upload_js_nav
fi

# Build *.html
if [[ $1 == "web" ]]; then
    clj -m choices.build
fi

# Build *.html
if [[ $1 == "json" ]]; then
    clj -m choices.json
fi

if [[ $1 == "" ]]; then
    compile_upload_js
    compile_upload_js_nav
    clj -m choices.json
    clj -m choices.build
fi
