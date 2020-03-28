;; Fonction de calcul de l'indice de masse corporelle.
(defn compute-imc [poids taille]
  (/ poids (Math/pow (/ taille 100.0) 2)))

;; Vous pouvez modifier cette fonction pour la tester.
(defn resultat [reponse]
  (let [reponse
        ;; Calcul du facteur âge, de l'IMC et de son impact sur les
        ;; facteurs de pronostique défavorable
        (merge reponse
               {:imc             (compute-imc (:poids reponse)
                                              (:taille reponse))
                :moins-de-15-ans (if (< (:age reponse) 15) 1 0)
                :plus-de-50-ans  (if (> (:age reponse) 50) 1 0)})
        {:keys [fievre diarrhees toux mal-de-gorge anosmie
                imc moins-de-15-ans plus-de-50-ans
                facteurs-gravite-mineurs
                facteurs-gravite-majeurs
                facteurs-pronostique]} reponse
        facteurs-pronostique
        (if (>= imc 30)
          (inc facteurs-pronostique)
          facteurs-pronostique)]
    (cond
      ;; Branche 1
      (= moins-de-15-ans 1)
      (do (println "Branche: 1 (moins de 15 ans)")
          (println "FIN1"))
      ;; Branche 2
      (or (and (> fievre 0) (= toux 0))
          (and (> toux 0) (> mal-de-gorge 0))
          (and (> toux 0) (> anosmie 0))
          (and (> fievre 0) (> diarrhees 0)))
      (do (println "Branche: 2 (fièvre ou autres symptômes)")
          (cond (>= facteurs-gravite-majeurs 1)
                (println "FIN5")
                (= facteurs-pronostique 0)
                (if (= facteurs-gravite-mineurs 0)
                  (if (and (not= moins-de-15-ans 1)
                           (not= plus-de-50-ans 1))
                    (println "FIN2")
                    (println "FIN3"))
                  (println "FIN3"))
                (>= facteurs-pronostique 1)
                (cond (< facteurs-gravite-mineurs 2)
                      (println "FIN3")
                      (>= facteurs-gravite-mineurs 2)
                      (println "FIN4"))))
      ;; Branche 3
      (>= facteurs-gravite-majeurs 1)
      (do (println "Branche: 3 (Un facteur majeur de gravité)")
          (println "FIN5: Appel du 15."))
      ;; Branche 4
      (and (> fievre 0) (> toux 0))
      (do (println "Branche: 4 (Fièvre et toux)")
          (if (= facteurs-pronostique 0)
            (when (>= facteurs-gravite-mineurs 0)
              (println "FIN6"))
            (when (>= facteurs-pronostique 1)
              (cond (>= facteurs-gravite-mineurs 0)
                    (println "FIN6")
                    (>= facteurs-gravite-mineurs 2)
                    (println "FIN4")))))
      ;; Branche 5
      (and (= fievre 0)
           (or (> toux 0) (> mal-de-gorge 0) (> anosmie 0)))
      (do (println "Branche: 5 (Pas de fièvre et un autre symptôme)")
          (cond (= facteurs-gravite-mineurs 0)
                (println "FIN7")
                (or (>= facteurs-gravite-mineurs 1)
                    (>= facteurs-pronostique 1))
                (println "FIN8")))
      ;; Branche 6
      (and (= fievre 0) (= toux 0) (= mal-de-gorge 0) (= anosmie 0))
      (do (println "Branche: 6 (Pas de symptôme)")
          (println "FIN9")))))
