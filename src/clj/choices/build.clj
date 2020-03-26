(ns choices.build
  (:require [choices.vars :as vars]
            [choices.views :as views]))

(defn -main []
  (spit "docs/index.html" (views/default vars/index-fr-meta vars/index-fr-contents))
  (spit "docs/repl.html" (views/default vars/repl-fr-meta vars/repl-fr-contents true))
  (println "File docs/*.html generated"))
