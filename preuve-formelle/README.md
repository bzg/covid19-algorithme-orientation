# Programme de vérification formelle de l'algorithme d'orientation COVID19

Cet outil nécessite Python >= 3.6 et le paquet pip `z3-solver`.

```
pip3 install z3-solver
```

Puis il suffit de lancer

```
python covid.py
```

Si cela affiche "Pas de non-terminaison possible!", c'est bon : il
existe une preuve mathématique que quelles que soient les réponses au
questionnaire, l'algorithme renverra toujours vers une fin prédéfinie.

Si non, cela affiche les réponses aux questionnaires qui déclenchent
une faille dans l'algorithme.

Cet outil s'appuie sur [le pseudo code de référence](https://github.com/Delegation-numerique-en-sante/covid19-algorithme-orientation/blob/master/pseudo-code.org#arbre-de-d%C3%A9cision). 

Si ce pseudo code est mis à jour, le code Python qui appelle Z3 doit
l'être aussi pour refléter le nouvel arbre de décision.

