;; Fonction de calcul de l'indice de masse corporelle.
(defn compute-bmi [weight height]
  (/ weight (Math/pow (/ height 100.0) 2)))

;; Vous pouvez modifier cette fonction pour la tester.
(defn resultat [reponse]
  (let [reponse
        ;; Calcul du facteur âge, de l'BMI et de son impact sur les
        ;; facteurs de pronostique défavorable
        (merge reponse
               {:bmi (compute-bmi (:weight reponse)
                                  (:height reponse))}
               (cond (< (:age scores) 15)
                     {:age_less_15 1 :age_less_50 1
                      :age_less_70 1 :age_more_70 0}
                     (< (:age scores) 50)
                     {:age_less_15 0 :age_less_50 1
                      :age_less_70 1 :age_more_70 0}
                     (< (:age scores) 70)
                     {:age_less_15 0 :age_less_50 0
                      :age_less_70 1 :age_more_70 0}
                     :else
                     {:age_less_15 0 :age_less_50 0
                      :age_less_70 0 :age_more_70 1}))
        {:keys [fever diarrhees cough douleurs agueusia_anosmia
                bmi age_less_15 age_less_50 age_less_70 age_more_70
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
      (do (when println? (println "Branch 1: less than 15 years"))
          (println "FIN1"))
      ;; Branche 2
      (>= major-severity-factors 1)
      (do (when println? (println "Branch 2: at least one major gravity factor"))
          (println "FIN5"))
      ;; Branche 3
      (and (= fever 1) (= cough 1))
      (do (when println? (println "Branch 3: fever and cough"))
          (cond (= pronostic-factors 0)
                (println "FIN6")
                (>= pronostic-factors 1)
                (if (< minor-severity-factors 2)
                  (println "FIN6")
                  (println "FIN4"))))
      ;; Branche 4
      (or (= fever 1) (= diarrhea 1)
          (and (= cough 1) (= sore_throat_aches 1))
          (and (= cough 1) (= agueusia_anosmia 1)))
      (do (when println? (println "Branch 4: fever and other symptoms"))
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
      (or (= cough 1) (= sore_throat_aches 1) (= agueusia_anosmia 1))
      (do (when println? (println "Branch 5: no fever and one other symptom"))
          (if (= pronostic-factors 0)
            (println "FIN2")
            (println "FIN7")))
      ;; Branche 6
      (and (= cough 0) (= sore_throat_aches 0) (= agueusia_anosmia 0))
      (do (when println? (println "Branche 6: no symptom"))
          (println "FIN8")))))
