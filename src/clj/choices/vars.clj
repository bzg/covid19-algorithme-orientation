(ns choices.vars
  (:require [clojure.walk :as walk]
            [markdown-to-hiccup.core :as md]
            [choices.macros :refer [inline-yaml-resource]]))

(def bulma-class-replacements
  {:h1  :h1.title
   :h3  :h3.is-size-4
   :ul  :ul.list.is-size-5
   :ol  :ol.list.is-size-5
   :li  :li.list-item
   :p   :p.is-size-5
   :pre :pre.is-size-6})

(defn md-to-string [s]
  (-> s (md/md->hiccup) (md/component)))

(defn fix-ordered-map [om]
  (walk/prewalk (fn [n] (if (map? n) (into {} n) n)) om))

(def config (inline-yaml-resource "config.yml"))

(def conditional-score-outputs
  (fix-ordered-map (:conditional-score-outputs config)))

(def index-fr-meta
  (inline-yaml-resource "website/fr/index-meta.yml"))

(def algo-fr-meta
  (inline-yaml-resource "website/fr/algo-meta.yml"))

(def index-fr-contents
  (walk/prewalk-replace
   bulma-class-replacements
   (md-to-string (slurp "website/fr/index-contents.md"))))

(def algo-fr-contents
  (walk/prewalk-replace
   bulma-class-replacements
   (md-to-string (slurp "website/fr/algorithme-orientation-covid19.md"))))

(def repl-fr-meta
  (inline-yaml-resource "website/fr/repl-meta.yml"))
