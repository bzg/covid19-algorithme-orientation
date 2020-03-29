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
      (and (= fievre 1) (= toux 1))
      (do (println "Branche 2: fièvre et toux")
          (cond (>= facteurs-gravite-majeurs 1)
                (println "FIN5")
                (and (= facteurs-pronostiques 0)
                     (< facteurs-gravite-mineurs 2))
                (println "FIN6")
                :else ; >= 1 facteurs pronostiques
                (if (< facteurs-gravite-mineurs 2)
                  (println "FIN6")
                  (println "FIN4"))))
      ;; Branche 3
      (or (= fievre 1) (= diarrhees 1)
          (and (= toux 1) (= douleurs 1))
          (and (= toux 1) (= anosmie 1)))
      (do (println "Branche 3: fièvre ou autres symptômes")
          (cond (>= facteurs-gravite-majeurs 1)
                (println "FIN5")
                (and (= facteurs-pronostiques 0)
                     (= facteurs-gravite-mineurs 0))
                (if (not= plus-de-49-ans 1)
                  (println "FIN2")
                  (println "FIN3"))
                :else ; >= 1 facteurs pronostiques
                (if (< facteurs-gravite-mineurs 2)
                  (println "FIN3")
                  (println "FIN4"))))
      ;; Branche 4
      (and (= fievre 0)
           (or (= toux 1) (= douleurs 1) (= anosmie 1)))
      (do (println "Branche 4: pas de fièvre et autres symptômes")
          (if (or (>= facteurs-gravite-mineurs 1)
                  (= facteurs-pronostiques 1))
            (println "FIN8")
            (println "FIN7")))
      ;; Branche 5
      (and (= fievre 0) (= toux 0)
           (= douleurs 0) (> anosmie 0))
      (do (println "Branche 5: pas de symptômes")
          (println "FIN9")))))
