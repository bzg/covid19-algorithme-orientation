(ns choices.vars
  (:require [clojure.walk :as walk]
            [choices.views :as views]
            [hiccup.page :as h]
            [markdown-to-hiccup.core :as md]
            [choices.macros :refer [inline-yaml-resource]]))

(def bulma-class-replacements
  {:h1 :h1.title
   :ul :ul.list
   :li :li.list-item})

(defn md-to-string [s]
  (-> s (md/md->hiccup) (md/component)))

(def index-fr-meta (inline-yaml-resource "website/fr/index-meta.yml"))

(def index-fr-contents (walk/prewalk-replace
                        bulma-class-replacements
                        (md-to-string (slurp "website/fr/index-contents.md"))))

(def repl-fr-meta (inline-yaml-resource "website/fr/repl-meta.yml"))

(def repl-fr-contents
  [:div.language-klipse "(+ 1 2)"])
