(ns choices.json
  (:require [yaml.core :as yaml]
            [cheshire.core :as json]
            [clojure.walk :as walk]))

(defn remove-deep [data keys & [re]]
  (walk/prewalk
   (fn [node]
     (if (map? node)
       (let [node (if re
                    (into
                     {}
                     (filter (fn [[k _]] (not (re-matches re (name k)))) node))
                    node)]
         (apply dissoc node keys))
       node))
   data))

(defn algo-with-data-collection []
  (let [parsed-config   (yaml/parse-string (slurp "config-nav.yml"))
        tree            (map
                         (fn [branch]
                           (if (re-matches #"^(1\.1|^1\.2)$" (:node branch))
                             (merge branch {:choices "Saisie utilisateur"})
                             branch))
                         (remove-deep (:tree parsed-config) [:color]))
        score-variables (remove-deep (:score-variables parsed-config) [:display])
        conditional-score-outputs1
        (remove-deep (:conditional-score-outputs parsed-config)
                     [:priority] #"condition-\d+")]
    (spit "docs/json/variables-nav.json"
          (json/generate-string
           {:variables score-variables}))
    (spit "docs/json/questions-nav.json"
          (json/generate-string
           {:questions (map
                        (fn [n] (select-keys n [:node :text :choices]))
                        (remove #(= (:home-page %) true) tree))}))
    (spit "docs/json/conclusions-nav.json"
          (json/generate-string
           {:conclusions conditional-score-outputs1}))))

(defn algo-without-data-collection []
  (let [parsed-config   (yaml/parse-string (slurp "config.yml"))
        tree            (map
                         (fn [branch]
                           (if (re-matches #"^2\.[12]$" (:node branch))
                             (merge branch {:choices "Saisie utilisateur"})
                             branch))
                         (remove-deep (:tree parsed-config) [:color]))
        score-variables (remove-deep (:score-variables parsed-config) [:display])
        conditional-score-outputs1
        (remove-deep (:conditional-score-outputs parsed-config)
                     [:priority] #"condition-\d+")]
    (spit "docs/json/variables.json"
          (json/generate-string
           {:variables score-variables}))
    (spit "docs/json/questions.json"
          (json/generate-string
           {:questions (map
                        (fn [n] (select-keys n [:node :text :choices]))
                        (remove #(= (:home-page %) true) tree))}))
    (spit "docs/json/conclusions.json"
          (json/generate-string
           {:conclusions conditional-score-outputs1}))
    (spit "docs/json/conclusions-with-priority.json"
          (json/generate-string
           {:conclusions-with-priority (:conditional-score-outputs parsed-config)}))))

(defn -main []
  (algo-with-data-collection)
  (algo-without-data-collection)
  (println "Files docs/json/*.json generated"))
