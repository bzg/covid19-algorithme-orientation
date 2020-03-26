(ns choices.algo
  (:require
   [clojure.test :refer :all]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [choices.macros :refer [inline-yaml-resource]]))

;; General configuration
(def config (inline-yaml-resource "config.yml"))
(def conditional-score-outputs (:conditional-score-outputs config))

(s/def ::moins-de-15-ans (s/int-in 0 2))
(s/def ::plus-de-50-ans (s/int-in 0 2))
(s/def ::poids (s/int-in 40 200)) ;; kgs
(s/def ::taille (s/int-in 120 240)) ;; cm
(s/def ::toux (s/int-in 0 2))
(s/def ::fievre (s/int-in 0 2))
(s/def ::anosmie (s/int-in 0 2))
(s/def ::mal-de-gorge (s/int-in 0 2))
(s/def ::diarrhees (s/int-in 0 2))
(s/def ::facteurs-pronostique (s/int-in 0 12))
(s/def ::facteurs-gravite-mineurs (s/int-in 0 4))
(s/def ::facteurs-gravite-majeurs (s/int-in 0 4))

(defn compute-imc [p t] (/ p (Math/pow (/ t 100.0) 2)))

(s/def ::reponse (s/keys :req-un [::moins-de-15-ans
                                  ::plus-de-50-ans
                                  ::poids
                                  ::taille
                                  ::fievre
                                  ::toux
                                  ::anosmie
                                  ::mal-de-gorge
                                  ::diarrhees
                                  ::facteurs-pronostique
                                  ::facteurs-gravite-mineurs
                                  ::facteurs-gravite-majeurs]))

(defn preprocess-scores [scores]
  (let [imc-val (compute-imc (:poids scores)
                             (:taille scores))
        imc-map {:imc imc-val}
        scores  (merge scores imc-map)
        scores  (update-in scores [:facteurs-pronostique]
                           #(if (> imc-val 30) (inc %) %))]
    ;; Returned preprocessed scores:
    scores))

(defn conditional-score-result [resultats]
  (let [conclusions
        conditional-score-outputs
        resultats
        (preprocess-scores resultats)
        {:keys [moins-de-15-ans plus-de-50-ans
                fievre toux anosmie mal-de-gorge diarrhees
                facteurs-gravite-mineurs facteurs-gravite-majeurs
                facteurs-pronostique]}                         resultats
        ;; Set the possible conclusions
        {:keys [FIN1 FIN2 FIN3 FIN4 FIN5 FIN6 FIN7 FIN8 FIN9]} conclusions
        ;; Set the final conclusion to one of the FIN*
        conclusion
        (cond
          ;; Branche 1
          (= moins-de-15-ans 1)
          (do (println "Moins de 15 ans => ") FIN1)
          ;; Branche 2
          (or (> fievre 0)
              (and (> toux 0) (> mal-de-gorge 0))
              (and (> toux 0) (> anosmie 0))
              (and (> fievre 0) (> diarrhees 0)))
          (do (println "Branche 2")
              (cond (>= facteurs-gravite-majeurs 1) FIN5
                    (= facteurs-pronostique 0)
                    (if (= facteurs-gravite-mineurs 0)
                      (if (= plus-de-50-ans 0) FIN2
                          FIN3)
                      FIN3)
                    (>= facteurs-pronostique 1)
                    (cond (< facteurs-gravite-mineurs 2)  FIN3 ;; Sure?
                          (>= facteurs-gravite-mineurs 2) FIN4)))
          ;; Branche 3
          (>= facteurs-gravite-majeurs 1)
          (do (println "Branche 3") FIN5)
          ;; Branche 4
          (and (> fievre 0) (> toux 0))
          (do (println "Branche 4")
              (if (= facteurs-pronostique 0)
                (when (> facteurs-gravite-mineurs 0) FIN6)
                (when (>= facteurs-pronostique 1)
                  (cond (>= facteurs-gravite-mineurs 0) FIN6
                        (>= facteurs-gravite-mineurs 2) FIN4))))
          ;; Branche 5
          (and (< fievre 0)
               (or (> toux 0) (> mal-de-gorge 0) (> anosmie 0)))
          (do (println "Branche 5")
              (cond (= facteurs-gravite-mineurs 0)                                   FIN7
                    (or (>= facteurs-gravite-mineurs 1) (>= facteurs-pronostique 1)) FIN8))
          ;; Branche 6
          (and (= fievre 0) (= toux 0) (= mal-de-gorge 0) (= anosmie 0))
          (do (println "Branche 6")
              FIN9))]
    ;; Return the expected map:
    {:res resultats
     :msg (get conclusion :message)}))

(defn -main []
  (doseq [exemple (gen/sample (s/gen ::reponse))]
    (let [{:keys [res msg]} (conditional-score-result exemple)]
      (println "Réponses: " res)
      (println "Conclusion: " msg))))


