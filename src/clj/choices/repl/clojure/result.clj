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
                :plus-de-49-ans  (if (> (:age reponse) 50) 1 0)})
        {:keys [fievre diarrhees toux douleurs anosmie
                imc moins-de-15-ans plus-de-49-ans
                facteurs-gravite-mineurs
                facteurs-gravite-majeurs
                facteurs-pronostique]} reponse
        facteurs-pronostique
        (if (>= imc 30)
          (inc facteurs-pronostique)
          facteurs-pronostique)]
    ;; L'algorithme COVID19 proprement dit.
    (cond
      ;; Branche 1
      (= moins-de-15-ans 1)
      (do (println "Branche 1: moins de 15 ans")
          (println "FIN1: Application non adaptée aux moins de 15 ans."))
      ;; Branche 2
      (>= facteurs-gravite-majeurs 1)
      (do (println "Branche 2: un facteur majeur de gravité")
          (println "FIN5: Appel du 15."))
      ;; Branche 3
      (> fievre 0)
      (do (println "Branche 3: fièvre et autre symptôme")
          (cond (> toux 0)
                (if (and (>= facteurs-pronostique 1)
                         (>= facteurs-gravite-mineurs 2))

                  (println "FIN4")
                  (println "FIN6"))

                (or (= diarrhees 0) (= douleurs 0) (= anosmie 0))
                (if (and (>= facteurs-pronostique 1)
                         (>= facteurs-gravite-mineurs 2))
                  (println "FIN4")
                  (if (or (= plus-de-49-ans 1)
                          (>= facteurs-gravite-mineurs 1))
                    (println "FIN3")
                    (println "FIN2")))))
      ;; Branche 4
      (or (> toux 0) (> douleurs 0) (> anosmie 0))
      (do (println "Branche 4: un autre symptôme sans fièvre")
          (if (or (>= facteurs-pronostique 1)
                  (>= facteurs-gravite-mineurs 1))
            (println "FIN8")
            (println "FIN7")))
      ;; Branche 6
      :else
      (do (println "Branche 6: Pas de symptôme")
          (println "FIN9")))))
