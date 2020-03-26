(ns choices.vars
  (:require [clojure.walk :as walk]
            [choices.views :as views]
            [hiccup.page :as h]
            [clojure.walk :as walk]
            [markdown-to-hiccup.core :as md]
            [choices.macros :refer [inline-yaml-resource]]))

(def bulma-class-replacements
  {:h1 :h1.title
   :ul :ul.list
   :li :li.list-item})

(defn md-to-string [s]
  (-> s (md/md->hiccup) (md/component)))

(defn fix-ordered-map [om]
  (walk/prewalk (fn [n] (if (map? n) (into {} n) n)) om))

(def config (inline-yaml-resource "config.yml"))

(def conditional-score-outputs (fix-ordered-map (:conditional-score-outputs config)))

(def index-fr-meta (inline-yaml-resource "website/fr/index-meta.yml"))

(def index-fr-contents (walk/prewalk-replace
                        bulma-class-replacements
                        (md-to-string (slurp "website/fr/index-contents.md"))))

(def repl-fr-meta (inline-yaml-resource "website/fr/repl-meta.yml"))

(def exemple-de-reponse ";; Vous pouvez modifier les valeurs ci-dessous.
{:fievre                   1
:diarrhees                0
:toux                     1
:mal-de-gorge             0
:anosmie                  0
:age                      62
:poids                    63
:taille                   167
:facteurs-pronostique     2
:facteurs-gravite-mineurs 1
:facteurs-gravite-majeurs 0}")

(def resultat ";; Vous pouvez modifier cette fonction pour la tester.
  (defn resultat [reponse]
  (let [reponse
        ;; Calcul du facteur âge, de l'IMC et de son impact sur les
        ;; facteurs de pronostique défavorable
        (merge reponse
               {:imc             (compute-imc (:poids reponse)
                                              (:taille reponse))
                :moins-de-15-ans (< (:age reponse) 15)
                :plus-de-50-ans  (> (:age reponse) 50)})
        {:keys [fievre diarrhees toux mal-de-gorge anosmie
                imc taille moins-de-15-ans plus-de-50-ans
                facteurs-gravite-mineurs
                facteurs-gravite-majeurs
                facteurs-pronostique]} reponse
        facteurs-pronostique
        (if (> imc 30) (inc facteurs-pronostique) facteurs-pronostique)]
    (cond
      ;; Branche 1
      (= moins-de-15-ans 1)
      (do (println \"Branche: 1 (moins de 15 ans)\")
          (println \"FIN1: Prenez contact avec votre médecin généraliste au moindre doute. Cette application n’est pour l’instant pas adaptée aux personnes de moins de 15 ans. En cas d’urgence, appeler le 15.\"))
      ;; Branche 2
      (or (and (> fievre 0) (= toux 0))
          (and (> toux 0) (> mal-de-gorge 0))
          (and (> toux 0) (> anosmie 0))
          (and (> fievre 0) (> diarrhees 0)))
      (do (println \"   Branche: 2 (fièvre ou autres symptômes)\")
          (cond (>= facteurs-gravite-majeurs 1)
                (println \"FIN5: Appel du 15.\")
                (= facteurs-pronostique 0)
                (if (= facteurs-gravite-mineurs 0)
                  (if (= plus-de-50-ans 0)
                    (println \"FIN2: Nous vous conseillons de rester à votre domicile et de contacter votre médecin en cas d’apparition de nouveaux symptômes. Vous pourrez aussi utiliser à nouveau l’application pour réévaluer vos symptômes.\")
                    (println \"FIN3: Téléconsultation ou médecin généraliste ou visite à domicile (SOS médecins). Appelez le 15 si une gêne respiratoire ou des difficultés importantes pour s’alimenter ou boire pendant plus de 24h apparaissent.\"))
                  (println \"FIN3: Téléconsultation ou médecin généraliste ou visite à domicile (SOS médecins). Appelez le 15 si une gêne respiratoire ou des difficultés importantes pour s’alimenter ou boire pendant plus de 24h apparaissent.\"))
                (>= facteurs-pronostique 1)
                (cond (< facteurs-gravite-mineurs 2)
                      (println \"FIN3: Téléconsultation ou médecin généraliste ou visite à domicile (SOS médecins). Appelez le 15 si une gêne respiratoire ou des difficultés importantes pour s’alimenter ou boire pendant plus de 24h apparaissent.\")
                      (>= facteurs-gravite-mineurs 2)
                      (println \"FIN4: Téléconsultation ou médecin généraliste ou visite à domicile (SOS médecins). Si pas possible de le joindre ou téléCS : faite le 15.\"))))
      ;; Branche 3
      (>= facteurs-gravite-majeurs 1)
      (do (println \"Branche: 3 (Un facteur majeur de gravité)\")
          (println \"FIN5: Appel du 15.\"))
      ;; Branche 4
      (and (> fievre 0) (> toux 0))
      (do (println \"Branche: 4 (Fièvre et toux)\")
          (if (= facteurs-pronostique 0)
            (when (> facteurs-gravite-mineurs 0)
              (println \"FIN6: Téléconsultation ou médecin généraliste ou visite à domicile (SOS médecins).\"))
            (when (>= facteurs-pronostique 1)
              (cond (>= facteurs-gravite-mineurs 0)
                    (println \"FIN6: Téléconsultation ou médecin généraliste ou visite à domicile (SOS médecins).\")
                    (>= facteurs-gravite-mineurs 2)
                    (println \"FIN4: Téléconsultation ou médecin généraliste ou visite à domicile (SOS médecins). Si pas possible de le joindre ou téléCS : faite le 15.\")))))
      ;; Branche 5
      (and (= fievre 0)
           (or (> toux 0) (> mal-de-gorge 0) (> anosmie 0)))
      (do (println \"Branche: 5 (Pas de fièvre et un autre symptôme\")
          (cond (= facteurs-gravite-mineurs 0)
                (println \"FIN7: Votre situation ne relève probablement pas du Covid-19. Consultez votre médecin au moindre doute. Si de nouveaux symptomes apparaissent, refaites le test.\")
                (or (>= facteurs-gravite-mineurs 1) (>= facteurs-pronostique 1))
                (println \"FIN8: Votre situation ne relève probablement pas du Covid-19. Un avis médical est recommandé. Au moindre doute, appelez le 15.\")))
      ;; Branche 6
      (and (= fievre 0) (= toux 0) (= mal-de-gorge 0) (= anosmie 0))
      (do (println \"Branche: 6 (Pas de symptôme)\")
          (println \"FIN9: Votre situation ne relève probablement pas du Covid-19. N’hésitez pas à contacter votre médecin en cas de doute. Vous pouvez refaire le test en cas de nouveau symptôme pour réévaluer la situation. Pour toute information concernant le Covid-19, composer le 0 800 130 000.\")))))")

(def repl-fr-contents
  [:section
   [:div.container
    [:br]
    [:h1.title "Code de l'indice de masse corporelle"]
    [:pre.language-klipse
     ";; La taille est ici indiquée en centimètre.
(defn compute-imc [poids taille] (/ poids (Math/pow (/ taille 100.0) 2)))"]]

   [:div.container
    [:br]
    [:h1.title "Fonction de calcul du résultat"]
    [:pre.language-klipse resultat]]

   [:div.container
    [:br]
    [:h1.title "Variable avec un exemple de réponse"]
    [:pre.language-klipse
     (str "(def exemple-de-reponse " exemple-de-reponse ")")]]

   [:div.container
    [:br]
    [:h1.title "Essayez vous-même"]
    [:p.subtitle "Changez l'exemple de réponse ci-dessus et voyez le résultat."]
    [:pre.language-klipse "(resultat exemple-de-reponse)"]]])
