(ns choices.generateall
  (:require [choices.generateindex :refer [generate-website-index]]
            [choices.generatejson :refer [generate-json-files]]))

(defn -main []
  (generate-website-index)
  (generate-json-files))

