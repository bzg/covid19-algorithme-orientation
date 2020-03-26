(ns choices.repl.clojure.index
  (:require [clojure.tools.reader]))

(def exemple-de-reponse
  (slurp "src/clj/choices/repl/clojure/response.clj"))

(def resultat
  (slurp "src/clj/choices/repl/clojure/result.clj"))

(def conclusions
  (clojure.tools.reader/read-string
   (slurp "src/clj/choices/repl/clojure/conclusions.clj")))

(def repl-fr-contents
  [:section
   [:div.container
    [:br]
    [:h1.title "Fonction de calcul du résultat"]
    [:pre.language-klipse resultat]]

   [:div.container
    [:br]
    [:h1.title "Variable avec un exemple de réponse"]
    [:pre.language-klipse exemple-de-reponse]]

   [:div.container
    [:br]
    [:h1.title "Essayez vous-même"]
    [:p.subtitle "Changez l'exemple de réponse ci-dessus et voyez le résultat."]
    [:pre.language-klipse "(resultat exemple-de-reponse)"]]

   [:div.container
    [:br]
    [:h1.title "Réponses possibles"]
    [:ul.list
     (for [[k v] conclusions]
       ^{:key (pr-str v)}
       [:li.list-item
        [:span [:b (name k)] ": " v]])]]])
