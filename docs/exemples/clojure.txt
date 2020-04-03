(ns choices.custom)

;; This file can contain at least two defns:
;;
;; (defn preprocess-scores [scores] ...)
;; (defn conditional-score-result [scores conditional-score-outputs] ...)
;;
;; You cannot use other names than those.
;; You can add utility functions.

(defn compute-bmi [^number p ^number t]
  (.toFixed (/ p (Math/pow (/ t 100.0) 2)) 2))

(defn preprocess-scores [scores]
  (println "Scores à calculer" scores)
  (let [bmi-val (compute-bmi (:value (:weight scores))
                             (:value (:height scores)))
        bmi-map {:bmi {:value bmi-val :display "BMI"}}
        scores  (merge scores bmi-map)
        scores  (update-in scores [:pronostic-factors]
                           #(if (>= bmi-val 30) (inc %) %))]
    ;; Return preprocessed scores:
    scores))

;; Available variables:
;; age_less_15
;; age_less_50
;; age_less_70
;; age_more_70
;; weight
;; height
;; fever
;; cough
;; agueusia_anosmia
;; sore_throat_aches
;; diarrhea
;; minor-severity-factors
;; major-severity-factors
;; pronostic-factors
(defn conditional-score-result [resultats conclusions]
  (let [{:keys [age_less_15 age_less_50 age_less_70 age_more_70
                fever cough agueusia_anosmia sore_throat_aches diarrhea
                minor-severity-factors
                major-severity-factors
                pronostic-factors]}                            resultats
        ;; Set the possible conclusions
        {:keys [FIN1 FIN2 FIN3 FIN4 FIN5 FIN6 FIN7 FIN8 FIN9]} conclusions
        ;; Set the final conclusion to one of the FIN*
        conclusion
        (cond
          ;; Branche 1
          (= age_less_15 1)
          FIN1
          ;; Branche 2
          (>= major-severity-factors 1)
          FIN5
          ;; Branche 3
          (and (= fever 1) (= cough 1))
          (cond (= pronostic-factors 0)
                FIN6
                (>= pronostic-factors 1)
                (if (< minor-severity-factors 2)
                  FIN6
                  FIN4))
          ;; Branche 4
          (or (= fever 1) (= diarrhea 1)
              (and (= cough 1) (= sore_throat_aches 1))
              (and (= cough 1) (= agueusia_anosmia 1)))
          (cond (= pronostic-factors 0)
                (if (= minor-severity-factors 0)
                  (if (= age_less_50 1)
                    FIN2
                    FIN3)
                  FIN3)
                (>= pronostic-factors 1)
                (if (< minor-severity-factors 2)
                  FIN3
                  FIN4))
          ;; Branche 5
          (or (= cough 1) (= sore_throat_aches 1) (= agueusia_anosmia 1))
          (if (= pronostic-factors 0)
            FIN2
            FIN7)
          ;; Branche 6
          (and (= cough 0) (= sore_throat_aches 0) (= agueusia_anosmia 0))
          FIN8)]
    ;; Return the expected map:
    {:notification (get conclusion :notification)
     :stick-help   (get conclusion :sticky-help)
     :node         (get conclusion :node)
     :output       (get conclusion :message)}))
