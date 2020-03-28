;; Vous pouvez modifier les valeurs ci-dessous.
(def exemple-de-reponse
  {:fievre                   1   ; 0 (pas de fièvre) ou 1 (fièvre)
   :diarrhees                0   ; Idem
   :toux                     1   ; Idem
   :douleurs                 0   ; Idem
   :anosmie                  0   ; Idem
   :age                      62  ; Valeur normale
   :poids                    63  ; En kgs
   :taille                   167 ; En centimètres
   :facteurs-pronostique     2   ; Voir première série de questions
   :facteurs-gravite-mineurs 1   ; Fièvre >= 39°C ou alitement >= 50% temps diurne
   :facteurs-gravite-majeurs 0   ; Gêne respiratoire ou difficulté manger/boire
   })
