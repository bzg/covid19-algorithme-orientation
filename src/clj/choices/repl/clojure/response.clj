;; Vous pouvez modifier les valeurs ci-dessous.
(def exemple-de-reponse
  {:fever                  1   ; 0 (pas de fièvre) ou 1 (fièvre)
   :diarrhea               0   ; Idem
   :cough                  1   ; Idem
   :sore_throat_aches      0   ; Idem
   :agueusia_anosmia       0   ; Idem
   :age                    62  ; Valeur normale
   :weight                 63  ; En kgs
   :height                 167 ; En centimètres
   :pronostic-factors      2   ; Voir première série de questions
   :minor-severity-factors 1   ; Fièvre >= 39°C ou alitement >= 50% temps diurne
   :major-severity-factors 0   ; Gêne respiratoire ou difficulté manger/boire
   })
