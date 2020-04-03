;; Fonction de calcul de l'indice de masse corporelle.
(defn compute-bmi [weight height]
  (/ weight (Math/pow (/ height 100.0) 2)))

(defn get-age [scores]
  (cond (< (:age scores) 15)
        {:age_less_15 true :age_less_50 false}
        (< (:age scores) 50)
        {:age_less_15 false :age_less_50 true}
        (< (:age scores) 70)
        {:age_less_15 false :age_less_50 false}
        :else
        {:age_less_15 false :age_less_50 false}))

;; Vous pouvez modifier cette fonction pour la tester.
(defn resultat [reponse]
  (let [reponse
        ;; Calcul du facteur âge, de l'BMI et de son impact sur les
        ;; facteurs de pronostique défavorable
        (merge reponse
               {:bmi (compute-bmi (:weight reponse)
                                  (:height reponse))}
               (get-age reponse))
        {:keys [fever diarrhea cough sore_throat_aches agueusia_anosmia
                bmi age_less_15 age_less_50
                minor-severity-factors
                major-severity-factors
                pronostic-factors]} reponse
        pronostic-factors
        (if (>= bmi 30)
          (inc pronostic-factors)
          pronostic-factors)]
    ;; L'algorithme COVID19 proprement dit.
    (cond
      ;; Branche 1
      (= age_less_15 1)
      (do (println "Branch 1: less than 15 years")
          (println "FIN1"))
      ;; Branche 2
      (>= major-severity-factors 1)
      (do (println "Branch 2: at least one major gravity factor")
          (println "FIN5"))
      ;; Branche 3
      (and fever cough)
      (do (println "Branch 3: fever and cough")
          (cond (= pronostic-factors 0)
                (println "FIN6")
                (>= pronostic-factors 1)
                (if (< minor-severity-factors 2)
                  (println "FIN6")
                  (println "FIN4"))))
      ;; Branche 4
      (or fever diarrhea
          (and cough sore_throat_aches)
          (and cough agueusia_anosmia))
      (do (println "Branch 4: fever and other symptoms")
          (cond (= pronostic-factors 0)
                (if (= minor-severity-factors 0)
                  (if (= age_less_50 1)
                    (println "FIN2")
                    (println "FIN3"))
                  (println "FIN3"))
                (>= pronostic-factors 1)
                (if (< minor-severity-factors 2)
                  (println "FIN3")
                  (println "FIN4"))))
      ;; Branche 5
      (or cough sore_throat_aches agueusia_anosmia)
      (do (println "Branch 5: no fever and one other symptom")
          (if (= pronostic-factors 0)
            (println "FIN2")
            (println "FIN7")))
      ;; Branche 6
      (and (not cough)
           (not sore_throat_aches)
           (not agueusia_anosmia))
      (do (println "Branche 6: no symptom")
          (println "FIN8")))))
