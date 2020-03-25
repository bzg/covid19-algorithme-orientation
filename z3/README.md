# Covid-19 vérification questionnaire

Utilise python >= 3.6 et pe paquet pip `z3-solver`. Il suffit de lancer

```
python covid.py
```

Si cela affiche, "Pas de non-terminaison possible!", c'est bon : il existe
une preuve mathématique que quelque soient les réponses au questionnaire, alors
l'algorithme renverra toujours vers une fin prédéfinie.

Si non, cela affiche les réponses aux questionnaires qui déclenchent une faille dans
l'algorithme.

Il faut mettre à jour le code Python qui appelle Z3 en fonction du pseudo-code
de l'arbre de décision.
