(ns choices.utils
  (:require [yaml.core :as yaml]
            [cheshire.core :as json]))

(defn generate-json-from-config []
  (spit
   "config.json"
   (json/generate-string
    (filter (fn [[a b]]
              (or (= a :tree)
                  (= a :score-variables)
                  (= a :conditional-score-output)))
            (yaml/parse-string (slurp "config.yml"))))))
