# coding=utf8

from z3 import *

# D'abord, il faut déclarer créer une instance du solveur Z3
s = Solver()

# On déclare toutes les entrées du problèmes, booléennes ou entières
age = Int("age")
fievre = Bool("fievre")
toux = Bool("toux")
mal_de_gorge = Bool("mal_de_gorge")
anosmie = Bool("anosmie")
diarrhee = Bool("diarrhee")
facteurs_pronostiques = Int("facteurs_pronostiques")
facteurs_gravite_mineur = Int("facteurs_gravite_mineur")
facteurs_gravite_majeur = Int("facteurs_gravite_majeur")

# Le solveur Z3 fonctionne en raisonnant sur un ensemble de contraintes portant
# sur les variables du problèmes. On ajoute des contraintes avec s.add()
# Ici, les contraintes sur les entrées.
s.add(age >= 0)
s.add(facteurs_pronostiques >= 0)
s.add(facteurs_gravite_mineur >= 0)
s.add(facteurs_gravite_mineur <= 2)
s.add(facteurs_gravite_majeur >= 0)
s.add(facteurs_gravite_majeur <= 2)

# Contraintes sur la définition des facteurs gravité mineurs : la fièvre en
# fait partie.
s.add(Implies(fievre, facteurs_gravite_mineur > 0))
s.add(Implies(facteurs_gravite_mineur == 2, fievre))

# Liste des variables du problème
variables = [age, fievre, toux, mal_de_gorge, anosmie, diarrhee,
             facteurs_pronostiques, facteurs_gravite_majeur, facteurs_gravite_mineur]

# L'algotithme définit 10 cas de terminaison que l'on stockera dans cette liste
# "done"
done = [False for i in range(1, 10)]

# La fonction suivante nous permet d'encoder comme une contrainte le fait
# de déclarer une condition de terminaison de l'algorithme.
# Ici "cond" est la condition à laquelle on veut que l'algorithme termine, et
# "number" le numéro du cas de terminaison.


def implies_done(cond, number):
    # L'algorithme est séquentiel : lorsque l'on atteint une condition de
    # terminaison, on arrête de l'exécuter. Or Z3 n'est pas séquentiel, les
    # contraintes ne fonctionnent pas comme ça. On a donc besoin ici d'un
    # accumulateur des conditions de terminaisons déjà rencontrées afin
    # d'émuler ce comportement séquentiel pour Z3.
    global implies_done_acc
    # On termine dans ce cas si la condition est remplie et que l'on est pas
    # dans un des cas de terminaison précédent.
    cond_acced = And(cond, Not(implies_done_acc))
    # On peut définit plusieurs conditions qui mènent au même cas de terminaison,
    # il est donc nécessaire de faire un "ou" logique avec le contenu de la
    # case de la liste.
    done[number - 1] = Or(done[number - 1], cond_acced)
    # On met à jour l'accumulateur
    implies_done_acc = Or(cond, implies_done_acc)


# L'accumulateur est initialisé à False car c'est une série de ou logiques.
implies_done_acc = False

# ## Encodage du pseudo-code dans Z3
# Le code Python ci-dessous est plutôt lisible, étant donné "implies_done"

#  SI moins de 15 ans => FIN1
implies_done(age <= 15, 1)
#
#  SI fièvre OU (toux ET mal de gorge) OU (toux ET anosmie) OU (fièvre ET diarrhée)
cond1 = Or(fievre, And(toux, mal_de_gorge), And(
    toux, anosmie), And(fievre, diarrhee))
#     SI 0 facteur pronostique
cond2 = (facteurs_pronostiques == 0)
#        SI 0 facteur de gravité mineur
cond3 = (facteurs_gravite_mineur == 0)
#           SI moins de 50 ans => FIN2
implies_done(And(cond1, cond2, cond3, age < 50), 2)
#           SINON => FIN3
implies_done(And(cond1, cond2, cond3, Not(age > 50)), 3)
#        SINON => FIN3
implies_done(And(cond1, cond2), 3)
#
#     SI 1 OU plus facteurs pronostique
cond4 = (facteurs_pronostiques > 0)
#        SI 0 OU 1 facteur de gravité mineur => FIN3
implies_done(And(cond1, cond4, Or(facteurs_gravite_mineur ==
                                  0, facteurs_gravite_mineur == 1)), 3)
#        SI au moins 2 facteurs de gravité mineurs => FIN4
implies_done(And(cond1, cond4, facteurs_gravite_mineur > 1), 4)
#
#     SI un facteur de gravité majeur => FIN5
implies_done(And(cond1, facteurs_gravite_majeur > 0), 5)
#
#  SI fièvre ET toux
cond5 = And(fievre, toux)
#     SI 0 facteur pronostique
cond6 = (facteurs_pronostiques == 0)
#        SI 0 OU 1 facteur de gravité mineurs => FIN6
implies_done(And(cond5, cond6, Or(facteurs_gravite_mineur ==
                                  0, facteurs_gravite_mineur == 1)), 6)
#     SI 1 OU plus facteur pronostique
cond7 = (facteurs_pronostiques > 0)
#        SI 0 OU 1 facteur de gravité mineurs => FIN6
implies_done(And(cond5, cond7, Or(facteurs_gravite_mineur ==
                                  0, facteurs_gravite_mineur == 1)), 6)
#        SI au moins 2 facteurs de gravité mineurs => FIN4
implies_done(And(cond5, cond7, facteurs_gravite_mineur > 1), 4)
#
#  SI 1 OU plus facteur de gravité majeur => FIN5
implies_done(facteurs_gravite_majeur > 0, 5)
#
#  SI toux OU mal de gorge OU anosmie ET pas de fièvre
# (ici doute sur la manière de parser le dernier ET)
cond8_alt1 = Or(toux, mal_de_gorge, And(anosmie, Not(fievre)))
cond8_alt2 = And(Or(toux, mal_de_gorge, anosmie), Not(fievre))
cond8 = cond8_alt1
#     SI 0 facteur de gravité mineur => FIN7
implies_done(And(cond8, facteurs_gravite_mineur == 0), 7)
#     SI au moins un facteur de gravité mineur OU un facteur pronostique => FIN8
implies_done(
    And(cond8, Or(facteurs_gravite_mineur > 0, facteurs_pronostiques > 0)), 8)
#
#  SI NI toux NI mal de gorge NI anosmie NI fièvre => FIN9
implies_done(And(Not(toux), Not(mal_de_gorge), Not(anosmie), Not(fievre)), 9)

# Maintenant que l'algorithme est encodé dans Z3, on peut essayer de prouver
# les théorèmes. Cette fonction wrappe la fonction "check" de Z3 avec une
# interface qui affiche la valeur des variables du contre-exemple si le théorème
# que l'on essaye de prouver est faux.
def check_and_print():
    c = s.check()
    if c == sat:
        print("Théorème faux, contre-exemple :")
        print("  Valeur des variables :")
        variables_evaluated = ["    {}: {}".format(
            v, s.model().evaluate(v, model_completion=True)) for v in variables]
        print(*variables_evaluated, sep="\n")
        print("  Sorties activées :")
        done_evaluated = [("    FIN{}".format(
            i + 1), s.model().evaluate(d, model_completion=True)) for (i, d) in enumerate(done)]
        done_evaluated = [a for (a, b) in done_evaluated if b]
        print(*done_evaluated, sep="\n")
    elif c == unsat:
        print("Théorème prouvé !\n")


print("Théorème: l'algorithme termine dans tous les cas")
s.push()
# Comment encoder ce théorème dans Z3 ? On se rappelle que "done" contient
# la liste de tous les cas de terminaisons, exprimés comme des formules logiques
# en fonction des variables d'entrée.
# Pour savoir si l'on termine dans tous les cas, il suffit de faire un "ou"
# logique de toutes les conditions de terminaison. En effet, si l'on passe
# la négation de ce "ou" à Z3, alors le solveur va essayer de trouver une
# combinaison des variables d'entrées telle que l'on ne soit dans aucun des
# cas de terminaison prévu. Si le solveur ne trouve pas de telle combinaison,*
# alors le théorème est prouvé.
cond = Or(done)
s.add(Not(cond))
check_and_print()
s.pop()

print("Théorème: les conditions de sortie sont mutuellement exclusives")
s.push()
# Ici, il s'agit de vérifier qu'il n'existe pas de manière de quitter l'algorithme
# avec plusieurs cas de terminaison déclenchés. Cette propriété est triviale
# pour un pseudo-code séquentiel (quand on termine, on arrête d'exécute),
# donc ici ce théorème sert surtout à montrer que la manière dont on a émulé
# le comportement séquentiel à Z3 est bien cohérente.
for j in range(0, 9):
    for i in range(0, j):
        cond = Not(And(done[i], done[j]))
        s.add(Not(cond))
check_and_print()
s.pop()
