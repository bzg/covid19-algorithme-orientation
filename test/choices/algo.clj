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

(s/def ::age (s/int-in 14 120))
(s/def ::weight (s/int-in 40 200)) ;; kgs
(s/def ::height (s/int-in 120 240)) ;; cm
(s/def ::cough (s/int-in 0 2))
(s/def ::fever (s/int-in 0 2))
(s/def ::agueusia_anosmia (s/int-in 0 2))
(s/def ::sore_throat_aches (s/int-in 0 2))
(s/def ::diarrhea (s/int-in 0 2))
(s/def ::pronostic-factors (s/int-in 0 12))
(s/def ::minor-severity-factors (s/int-in 0 4))
(s/def ::major-severity-factors (s/int-in 0 4))

(defn compute-bmi [p t] (/ p (Math/pow (/ t 100.0) 2)))

(s/def ::reponse (s/keys :req-un [::age
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

(defn preprocess-scores [scores]
  (let [bmi-val (compute-bmi (:weight scores)
                             (:height scores))
        bmi-map {:bmi bmi-val}
        scores  (merge scores
                       (cond (< (:age scores) 15)
                             {:age_less_15 1 :age_less_50 0
                              :age_less_70 0 :age_more_70 0}
                             (< (:age scores) 50)
                             {:age_less_15 0 :age_less_50 1
                              :age_less_70 1 :age_more_70 0}
                             (< (:age scores) 70)
                             {:age_less_15 0 :age_less_50 0
                              :age_less_70 1 :age_more_70 0}
                             :else
                             {:age_less_15 0 :age_less_50 0
                              :age_less_70 0 :age_more_70 1}))
        scores  (merge scores bmi-map)
        scores  (update-in scores [:pronostic-factors]
                           #(if (>= bmi-val 30) (inc %) %))
        scores  (dissoc scores :weight :height :age)]
    ;; Returned preprocessed scores:
    scores))

(defn preprocess-scores-no-println [scores]
  (let [scores (merge scores
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
        scores (update-in scores [:pronostic-factors]
                          #(if (>= (:bmi scores) 30) (inc %) %))
        scores (update-in scores [:pronostic-factors]
                          #(if (>= (:age scores) 70) (inc %) %))
        scores (dissoc scores :weight :height :age)]
    ;; Returned preprocessed scores:
    scores))

(defn conditional-score-result [response & [println?]]
  (let [;; Set the possible conclusions
        {:keys [FIN1 FIN2 FIN3 FIN4 FIN5 FIN6 FIN7 FIN8 FIN9]}
        conditional-score-outputs
        response
        (if println?
          (preprocess-scores response)
          (preprocess-scores-no-println response))
        {:keys [age_less_15 age_less_50 age_less_70 age_more_70
                fever cough agueusia_anosmia sore_throat_aches diarrhea
                minor-severity-factors
                major-severity-factors
                pronostic-factors]} response
        ;; Set the final conclusion to one of the FIN*
        conclusion
        (cond
          ;; Branche 1
          (= age_less_15 1)
          (do (when println? (println "Branch 1: less than 15 years"))
              FIN1)
          ;; Branche 2
          (>= major-severity-factors 1)
          (do (when println? (println "Branch 2: at least one major gravity factor"))
              FIN5)
          ;; Branche 3
          (and (= fever 1) (= cough 1))
          (do (when println? (println "Branch 3: fever and cough"))
              (cond (= pronostic-factors 0)
                    FIN6
                    (>= pronostic-factors 1)
                    (if (< minor-severity-factors 2)
                      FIN6
                      FIN4)))
          ;; Branche 4
          (or (= fever 1) (= diarrhea 1)
              (and (= cough 1) (= sore_throat_aches 1))
              (and (= cough 1) (= agueusia_anosmia 1)))
          (do (when println? (println "Branch 4: fever and other symptoms"))
              (cond (= pronostic-factors 0)
                    (if (= minor-severity-factors 0)
                      (if (= age_less_50 1)
                        FIN2
                        FIN3)
                      FIN3)
                    (>= pronostic-factors 1)
                    (if (< minor-severity-factors 2)
                      FIN3
                      FIN4)))
          ;; Branche 5
          (or (= cough 1) (= sore_throat_aches 1) (= agueusia_anosmia 1))
          (do (when println? (println "Branch 5: no fever and one other symptom"))
              (if (= pronostic-factors 0)
                FIN2
                FIN7))
          ;; Branche 6
          (and (= cough 0) (= sore_throat_aches 0) (= agueusia_anosmia 0))
          (do (when println? (println "Branche 6: no symptom"))
              FIN8))]
    ;; Return the expected map:
    {:res response
     :msg (get conclusion :message)}))

(def all-inputs
  (let [bin   (fd/interval 0 1)
        multi (fd/interval 0 2)]
    (logic/run* [q]
      (logic/fresh [fever cough agueusia_anosmia
                    sore_throat_aches diarrhea
                    pronostic-factors
                    minor-severity-factors
                    major-severity-factors
                    age bmi
                    response]
        (fd/in age (fd/interval 14 70))
        (fd/in fever bin)
        (fd/in cough bin)
        (fd/in agueusia_anosmia bin)
        (fd/in sore_throat_aches bin)
        (fd/in diarrhea bin)
        (fd/in pronostic-factors (fd/interval 0 10))
        (fd/in minor-severity-factors multi)
        (fd/in major-severity-factors multi)
        (fd/in bmi multi)
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
        (logic/== q response)))))

(def all-results (map conditional-score-result all-inputs))
(def all-results-no-nil (remove nil? all-results))

(deftest conclusion?
  (testing "Is each input reaching a conclusion?"
    (is (= (count all-results) (count all-results-no-nil)))))

(deftest all-conclusions?
  (testing "Is each conclusion reached at least once?"
    (is (= (count (distinct (map :msg all-results))) 8))))

(defn -main [& [arg]]
  (let [samples (gen/sample
                 (s/gen ::reponse)
                 (or (and (not= "logic" arg) (not-empty arg) (Integer. arg)) 1))]
    (if (= arg "logic")
      (run-tests 'choices.algo)
      (doseq [sample samples]
        (let [{:keys [res msg]} (conditional-score-result sample true)]
          (println "Réponses: " res)
          (println "Conclusion: " msg))))))
