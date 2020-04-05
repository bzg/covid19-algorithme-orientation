(ns choices.algo
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.core.logic :as logic]
   [clojure.core.logic.fd :as fd]
   [clojure.test :refer [deftest is testing run-tests]]
   [choices.macros :refer [inline-yaml-resource]]))

;; General configuration
(def config (inline-yaml-resource "config.yml"))
(def conditional-score-outputs (:conditional-score-outputs config))

(s/def ::age (s/int-in 14 71))
(s/def ::weight (s/int-in 59 111)) ;; kgs
(s/def ::height (s/int-in 139 191)) ;; cm
(s/def ::cough boolean?)
(s/def ::fever boolean?)
(s/def ::agueusia_anosmia boolean?)
(s/def ::sore_throat_aches boolean?)
(s/def ::diarrhea boolean?)
(s/def ::pronostic-factors (s/int-in 0 12))
(s/def ::minor-severity-factors (s/int-in 0 3))
(s/def ::major-severity-factors (s/int-in 0 3))

(defn compute-bmi [p t] (/ p (Math/pow (/ t 100.0) 2)))

(s/def ::response (s/keys :req-un [::age
                                   ::weight
                                   ::height
                                   ::fever
                                   ::cough
                                   ::agueusia_anosmia
                                   ::sore_throat_aches
                                   ::diarrhea
                                   ::pronostic-factors
                                   ::minor-severity-factors
                                   ::major-severity-factors]))

(defn get-age [scores]
  (cond (< (:age scores) 15)
        {:age_less_15 true :age_less_50 false}
        (< (:age scores) 50)
        {:age_less_15 false :age_less_50 true}
        (< (:age scores) 70)
        {:age_less_15 false :age_less_50 true}
        :else
        {:age_less_15 false :age_less_50 true}))

(defn preprocess-scores [scores]
  (let [bmi-val (compute-bmi (:weight scores)
                             (:height scores))
        bmi-map {:bmi bmi-val}
        scores  (merge scores (get-age scores))
        scores  (merge scores bmi-map)
        scores  (update-in scores [:pronostic-factors]
                           #(if (>= bmi-val 30) (inc %) %))
        scores  (dissoc scores :weight :height :age)]
    ;; Returned preprocessed scores:
    scores))

(defn preprocess-scores-no-println [scores]
  (let [scores (merge scores (get-age scores))
        scores (update-in scores [:pronostic-factors]
                          #(if (>= (:bmi scores) 30) (inc %) %))
        scores (update-in scores [:pronostic-factors]
                          #(if (>= (:age scores) 70) (inc %) %))
        scores (dissoc scores :weight :height :age)]
    ;; Returned preprocessed scores:
    scores))

(defn conditional-score-result [response & [println?]]
  (let [;; Set the possible conclusions
        {:keys [orientation_moins_de_15_ans
                orientation_domicile_surveillance_1
                orientation_consultation_surveillance_1
                orientation_consultation_surveillance_2
                orientation_SAMU
                orientation_consultation_surveillance_3
                orientation_consultation_surveillance_4
                orientation_surveillance FIN9]}
        conditional-score-outputs
        response
        (if println?
          (preprocess-scores response)
          (preprocess-scores-no-println response))
        {:keys [age_less_15 age_less_50
                fever cough agueusia_anosmia sore_throat_aches diarrhea
                minor-severity-factors
                major-severity-factors
                pronostic-factors]} response
        ;; Set the final conclusion to one of the FIN*
        conclusion
        (cond
          ;; Branche 1
          age_less_15
          (do (when println? (println "Branch 1: less than 15 years"))
              orientation_moins_de_15_ans)
          ;; Branche 2
          (>= major-severity-factors 1)
          (do (when println? (println "Branch 2: at least one major gravity factor"))
              orientation_SAMU)
          ;; Branche 3
          (and fever cough)
          (do (when println? (println "Branch 3: fever and cough"))
              (cond (= pronostic-factors 0)
                    orientation_consultation_surveillance_3
                    (>= pronostic-factors 1)
                    (if (< minor-severity-factors 2)
                      orientation_consultation_surveillance_3
                      orientation_consultation_surveillance_2)))
          ;; Branche 4
          (or fever diarrhea
              (and cough sore_throat_aches)
              (and cough agueusia_anosmia))
          (do (when println? (println "Branch 4: fever and other symptoms"))
              (cond (= pronostic-factors 0)
                    (if (= minor-severity-factors 0)
                      (if age_less_50
                        orientation_domicile_surveillance_1
                        orientation_consultation_surveillance_1)
                      orientation_consultation_surveillance_1)
                    (>= pronostic-factors 1)
                    (if (< minor-severity-factors 2)
                      orientation_consultation_surveillance_1
                      orientation_consultation_surveillance_2)))
          ;; Branche 5
          (or cough sore_throat_aches agueusia_anosmia)
          (do (when println? (println "Branch 5: no fever and one other symptom"))
              (if (= pronostic-factors 0)
                orientation_domicile_surveillance_1
                orientation_consultation_surveillance_4))
          ;; Branche 6
          (and (not cough)
               (not sore_throat_aches)
               (not agueusia_anosmia))
          (do (when println? (println "Branche 6: no symptom"))
              orientation_surveillance))]
    ;; Return the expected map:
    {:res response
     :msg (get conclusion :message)}))

(def all-inputs
  (logic/run* [q]
    (logic/fresh [fever cough agueusia_anosmia
                  sore_throat_aches diarrhea
                  pronostic-factors
                  minor-severity-factors
                  major-severity-factors
                  age bmi
                  response]
      (fd/in age (fd/interval 14 71))
      (logic/membero fever [true false])
      (logic/membero cough [true false])
      (logic/membero agueusia_anosmia [true false])
      (logic/membero sore_throat_aches [true false])
      (logic/membero diarrhea [true false])
      (fd/in bmi (fd/interval 29 30))
      (fd/in pronostic-factors (fd/interval 0 12))
      (fd/in minor-severity-factors (fd/interval 0 2))
      (fd/in major-severity-factors (fd/interval 0 2))
      (logic/== response {:fever                  fever
                          :cough                  cough
                          :agueusia_anosmia       agueusia_anosmia
                          :age                    age
                          :sore_throat_aches      sore_throat_aches
                          :diarrhea               diarrhea
                          :bmi                    bmi
                          :pronostic-factors      pronostic-factors
                          :minor-severity-factors minor-severity-factors
                          :major-severity-factors major-severity-factors})
      (logic/== q response))))

(def all-results (map conditional-score-result all-inputs))
(def all-results-no-nil (remove nil? all-results))

(deftest each-input-as-a-conclusion?
  (testing "Is each input reaching a conclusion?"
    (is (= (count all-results) (count all-results-no-nil)))))

(deftest all-conclusions-are-reached?
  (testing "Is each conclusion reached at least once?"
    (is (= (count (distinct (map :msg all-results))) 8))))

(defn -main [& [arg]]
  (let [samples (gen/sample
                 (s/gen ::response)
                 (or (and (not= "logic" arg) (not-empty arg) (Integer. arg)) 1))]
    (if (= arg "logic")
      (run-tests 'choices.algo)
      (doseq [sample samples]
        (let [{:keys [res msg]} (conditional-score-result sample true)]
          (println "Réponses: " res)
          (println "Conclusion: " msg))))))
