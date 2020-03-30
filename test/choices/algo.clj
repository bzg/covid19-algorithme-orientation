(ns choices.algo
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [choices.macros :refer [inline-yaml-resource]]))

;; General configuration
(def config (inline-yaml-resource "config.yml"))
(def conditional-score-outputs (:conditional-score-outputs config))

(s/def ::moins-de-15-ans (s/int-in 0 2))
(s/def ::plus-de-49-ans (s/int-in 0 2))
(s/def ::poids (s/int-in 40 200)) ;; kgs
(s/def ::taille (s/int-in 120 240)) ;; cm
(s/def ::toux (s/int-in 0 2))
(s/def ::fievre (s/int-in 0 2))
(s/def ::anosmie (s/int-in 0 2))
(s/def ::douleurs (s/int-in 0 2))
(s/def ::diarrhees (s/int-in 0 2))
(s/def ::facteurs-pronostiques (s/int-in 0 12))
(s/def ::facteurs-gravite-mineurs (s/int-in 0 4))
(s/def ::facteurs-gravite-majeurs (s/int-in 0 4))

(defn compute-imc [p t] (/ p (Math/pow (/ t 100.0) 2)))

(s/def ::reponse (s/keys :req-un [::moins-de-15-ans
                                  ::plus-de-49-ans
                                  ::poids
                                  ::taille
                                  ::fievre
                                  ::toux
                                  ::anosmie
                                  ::douleurs
                                  ::diarrhees
                                  ::facteurs-pronostiques
                                  ::facteurs-gravite-mineurs
                                  ::facteurs-gravite-majeurs]))

(defn preprocess-scores [scores]
  (let [imc-val (compute-imc (:poids scores)
                             (:taille scores))
        imc-map {:imc imc-val}
        scores  (merge scores imc-map)
        scores  (update-in scores [:facteurs-pronostiques]
                           #(if (>= imc-val 30) (inc %) %))]
    ;; Returned preprocessed scores:
    scores))

(defn conditional-score-result [resultats]
  (let [conclusions
        conditional-score-outputs
        resultats
        (preprocess-scores resultats)
        {:keys [moins-de-15-ans plus-de-49-ans
                fievre toux anosmie douleurs diarrhees
                facteurs-gravite-mineurs facteurs-gravite-majeurs
                facteurs-pronostiques]}                        resultats
        ;; Set the possible conclusions
        {:keys [FIN1 FIN2 FIN3 FIN4 FIN5 FIN6 FIN7 FIN8 FIN9]} conclusions
        ;; Set the final conclusion to one of the FIN*
        conclusion
        (cond
          ;; Branche 1
          (= moins-de-15-ans 1)
          (do (println "Branche 1: moins de 15 ans")
              FIN1)
          ;; Branche 2
          (and (= fievre 1) (= toux 1))
          (do (println "Branche 2: fièvre et toux")
              (cond (>= facteurs-gravite-majeurs 1)
                    FIN5
                    (= facteurs-pronostiques 0)
                    FIN6
                    (>= facteurs-pronostiques 1)
                    (if (< facteurs-gravite-mineurs 2)
                      FIN6
                      FIN4)))
          ;; Branche 3
          (or (= fievre 1) (= diarrhees 1)
              (and (= toux 1) (= douleurs 1))
              (and (= toux 1) (= anosmie 1)))
          (do (println "Branche 3: fièvre ou autres symptômes")
              (cond (>= facteurs-gravite-majeurs 1)
                    FIN5
                    (= facteurs-pronostiques 0)
                    (if (= facteurs-gravite-mineurs 0)
                      (if (not= plus-de-49-ans 1)
                        FIN2
                        FIN3)
                      FIN3)
                    (>= facteurs-pronostiques 1)
                    (if (< facteurs-gravite-mineurs 2)
                      FIN3
                      FIN4)))
          ;; Branche 4
          (or (= toux 1) (= douleurs 1) (= anosmie 1))
          (do (println "Branche 4: pas de fièvre et un autre symptôme")
              (if (= facteurs-pronostiques 0)
                FIN2
                FIN7))
          ;; Branche 5
          (and (= toux 0) (= douleurs 0) (= anosmie 0))
          (do (println "Branche 5: pas de symptômes")
              FIN8))]
    ;; Return the expected map:
    {:res resultats
     :msg (get conclusion :message)}))

(defn -main [& [n]]
  (doseq [exemple (gen/sample (s/gen ::reponse)
                              (or (and (not-empty n) (Integer. n)) 1))]
    (let [{:keys [res msg]} (conditional-score-result exemple)]
      (println "Réponses: " res)
      (println "Conclusion: " msg))))
