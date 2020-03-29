(ns choices.build
  (:require [choices.vars :as vars]
            [choices.views :as views]
            [choices.repl.clojure.index :as repl.clojure.index]))

(defn -main []
  (spit "docs/index.html"
        (views/default
         vars/index-fr-meta
         vars/index-fr-contents))
  (spit "docs/algorithme-orientation-covid19.html"
        (views/default
         vars/algo-fr-meta
         vars/algo-fr-contents))
  (spit "docs/repl.html"
        (views/default
         vars/repl-fr-meta
         repl.clojure.index/repl-fr-contents true))
  (println "File docs/*.html generated"))
