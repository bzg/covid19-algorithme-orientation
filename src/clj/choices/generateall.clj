(ns choices.generateall
  (:require [choices.generateweb :refer [generate-web-index]]
            [choices.generatejson :refer [generate-json-files]]))

(defn -main []
  (generate-web-index)
  (generate-json-files))

