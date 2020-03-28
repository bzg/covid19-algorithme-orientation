(ns choices.custom)

;; This file can contain at least two defns:
;;
;; (defn preprocess-scores [scores] ...)
;; (defn conditional-score-result [scores conditional-score-outputs] ...)
;;
;; You cannot use other names than those.
;; You can add utility functions.

(defn compute-imc [^number p ^number t]
  (.toFixed (/ p (Math/pow t 2)) 2))

(defn preprocess-scores [scores]
  (let [imc-val (compute-imc (:value (:poids scores))
                             (:value (:taille scores)))
        imc-map {:imc {:display "IMC" :value imc-val}}
        scores  (merge scores imc-map)
        scores  (update-in scores [:facteurs-pronostique :value]
                           #(if (>= imc-val 30) (inc %) %))]
    ;; Returned preprocessed scores:
    scores))

;; Available variables:
;; moins-de-15-ans
;; plus-de-49-ans
;; poids
;; taille
;; fievre
;; toux
;; anosmie
;; douleurs
;; diarrhees
;; facteurs-gravite-mineurs
;; facteurs-gravite-majeurs
;; facteurs-pronostique
(defn conditional-score-result [resultats conclusions]
  (let [{:keys [moins-de-15-ans plus-de-49-ans
                fievre toux anosmie douleurs diarrhees
                facteurs-gravite-mineurs facteurs-gravite-majeurs
                facteurs-pronostique]}                         resultats
        ;; Set the possible conclusions
        {:keys [FIN1 FIN2 FIN3 FIN4 FIN5 FIN6 FIN7 FIN8 FIN9]} conclusions
        ;; Set the final conclusion to one of the FIN*
        conclusion
        (cond
          ;; Branche 1
          (= moins-de-15-ans 1)           FIN1
          ;; Branche 2
          (>= facteurs-gravite-majeurs 1) FIN5
          ;; Branche 3
          (> fievre 0)
          (cond (> toux 0)
                (if (and (>= facteurs-pronostique 1)
                         (>= facteurs-gravite-mineurs 2))
                  FIN4
                  FIN6)
                (or (= diarrhees 0) (= douleurs 0) (= anosmie 0))
                (if (and (>= facteurs-pronostique 1)
                         (>= facteurs-gravite-mineurs 2))
                  FIN4
                  (if (or (= plus-de-49-ans 1)
                          (>= facteurs-gravite-mineurs 1))
                    FIN3
                    FIN2)))
          ;; Branche 4
          (or (> toux 0) (> douleurs 0) (> anosmie 0))
          (if (or (>= facteurs-pronostique 1)
                  (>= facteurs-gravite-mineurs 1))
            FIN8
            FIN7)
          ;; Branche 6
          :else
          FIN9)]
    ;; Return the expected map:
    {:notification (get conclusion :notification)
     :output       (get conclusion :message)}))
