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
                facteurs-pronostiques]} reponse
        facteurs-pronostiques
        (if (>= imc 30)
          (inc facteurs-pronostiques)
          facteurs-pronostiques)]
    ;; L'algorithme COVID19 proprement dit.
    (cond
      ;; Branche 1
      (= moins-de-15-ans 1)
      (do (println "Branche 1: moins de 15 ans")
          (println "FIN1"))
      ;; Branche 2
      (>= facteurs-gravite-majeurs 1)
      (do (println "Branche 2: au moins un facteur de gravité majeur")
          (println "FIN5"))
      ;; Branche 3
      (and (= fievre 1) (= toux 1))
      (do (println "Branche 2: fièvre et toux")
          (cond (= facteurs-pronostiques 0)
                (println "FIN6")
                (>= facteurs-pronostiques 1)
                (if (< facteurs-gravite-mineurs 2)
                  (println "FIN6")
                  (println "FIN4"))))
      ;; Branche 4
      (or (= fievre 1) (= diarrhees 1)
          (and (= toux 1) (= douleurs 1))
          (and (= toux 1) (= anosmie 1)))
      (do (println "Branche 4: fièvre ou autres symptômes")
          (cond (= facteurs-pronostiques 0)
                (if (= facteurs-gravite-mineurs 0)
                  (if (not= plus-de-49-ans 1)
                    (println "FIN2")
                    (println "FIN3"))
                  (println "FIN3"))
                (>= facteurs-pronostiques 1)
                (if (< facteurs-gravite-mineurs 2)
                  (println "FIN3")
                  (println "FIN4"))))
      ;; Branche 5
      (or (= toux 1) (= douleurs 1) (= anosmie 1))
      (do (println "Branche 4: pas de fièvre et un autre symptôme")
          (if (= facteurs-pronostiques 0)
            (println "FIN2")
            (println "FIN7")))
      ;; Branche 6
      (and (= toux 0) (= douleurs 0) (= anosmie 0))
      (do (println "Branche 5: pas de symptômes")
          (println "FIN8")))))
