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

(s/def ::reponse (s/keys :req-un [::age
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
        scores  (merge scores
                       {:plus-de-49-ans  (if (> (:age scores) 49) 1 0)
                        :moins-de-15-ans (if (< (:age scores) 15) 1 0)})
        scores  (merge scores imc-map)
        scores  (update-in scores [:facteurs-pronostiques]
                           #(if (>= imc-val 30) (inc %) %))
        scores  (dissoc scores :poids :taille :age)]
    ;; Returned preprocessed scores:
    scores))

(defn preprocess-scores-no-println [scores]
  (let [scores (merge scores
                      {:plus-de-49-ans  (if (> (:age scores) 49) 1 0)
                       :moins-de-15-ans (if (< (:age scores) 15) 1 0)})
        scores (update-in scores [:facteurs-pronostiques]
                          #(if (>= (:imc scores) 30) (inc %) %))
        scores (update-in scores [:facteurs-pronostiques]
                          #(if (>= (:age scores) 70) (inc %) %))
        scores (dissoc scores :poids :taille :age)]
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
        {:keys [moins-de-15-ans plus-de-49-ans
                fievre toux anosmie douleurs diarrhees
                facteurs-gravite-mineurs facteurs-gravite-majeurs
                facteurs-pronostiques]} response
        ;; Set the final conclusion to one of the FIN*
        conclusion
        (cond
          ;; Branche 1
          (= moins-de-15-ans 1)
          (do (when println? (println "Branche 1: moins de 15 ans"))
              FIN1)
          ;; Branche 2
          (>= facteurs-gravite-majeurs 1)
          (do (when println? (println "Branche 2: au moins un facteur de gravité majeur"))
              FIN5)
          ;; Branche 3
          (and (= fievre 1) (= toux 1))
          (do (when println? (println "Branche 2: fièvre et toux"))
              (cond (= facteurs-pronostiques 0)
                    FIN6
                    (>= facteurs-pronostiques 1)
                    (if (< facteurs-gravite-mineurs 2)
                      FIN6
                      FIN4)))
          ;; Branche 4
          (or (= fievre 1) (= diarrhees 1)
              (and (= toux 1) (= douleurs 1))
              (and (= toux 1) (= anosmie 1)))
          (do (when println? (println "Branche 4: fièvre ou autres symptômes"))
              (cond (= facteurs-pronostiques 0)
                    (if (= facteurs-gravite-mineurs 0)
                      (if (not= plus-de-49-ans 1)
                        FIN2
                        FIN3)
                      FIN3)
                    (>= facteurs-pronostiques 1)
                    (if (< facteurs-gravite-mineurs 2)
                      FIN3
                      FIN4)))
          ;; Branche 5
          (or (= toux 1) (= douleurs 1) (= anosmie 1))
          (do (when println? (println "Branche 4: pas de fièvre et un autre symptôme"))
              (if (= facteurs-pronostiques 0)
                FIN2
                FIN7))
          ;; Branche 6
          (and (= toux 0) (= douleurs 0) (= anosmie 0))
          (do (when println? (println "Branche 5: pas de symptômes"))
              FIN8))]
    ;; Return the expected map:
    {:res response
     :msg (get conclusion :message)}))

(def all-inputs
  (let [bin   (fd/interval 0 1)
        multi (fd/interval 0 2)]
    (logic/run* [q]
      (logic/fresh [fievre toux anosmie
                    douleurs diarrhees
                    facteurs-pronostiques
                    facteurs-gravite-mineurs
                    facteurs-gravite-majeurs
                    age imc
                    response]
        (fd/in age (fd/interval 14 70))
        (fd/in fievre bin)
        (fd/in toux bin)
        (fd/in anosmie bin)
        (fd/in douleurs bin)
        (fd/in diarrhees bin)
        (fd/in facteurs-pronostiques (fd/interval 0 10))
        (fd/in facteurs-gravite-mineurs multi)
        (fd/in facteurs-gravite-majeurs multi)
        (fd/in imc multi)
        (logic/== response {:fievre                   fievre
                            :toux                     toux
                            :anosmie                  anosmie
                            :age                      age
                            :douleurs                 douleurs
                            :diarrhees                diarrhees
                            :imc                      imc
                            :facteurs-pronostiques    facteurs-pronostiques
                            :facteurs-gravite-mineurs facteurs-gravite-mineurs
                            :facteurs-gravite-majeurs facteurs-gravite-majeurs})
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
