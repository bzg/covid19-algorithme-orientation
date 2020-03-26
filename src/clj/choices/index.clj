(ns choices.index
  (:require [clojure.walk :as walk]
            [choices.views :as views]
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

(defn -main []
  (spit "docs/index.html" (views/default index-fr-meta index-fr-contents))
  (println "File docs/index.html generated")
  ;; (spit "docs/interactive.html" (default index-meta index-fr-contents))
  ;; (println "File docs/interactive.html generated")
  )
