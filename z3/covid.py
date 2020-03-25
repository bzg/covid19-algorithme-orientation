# coding=utf8

from z3 import *

s = Solver()

age = Int("age")
fievre = Bool("fievre")
toux = Bool("toux")
mal_de_gorge = Bool("mal_de_gorge")
anosmie = Bool("anosmie")
diarrhee = Bool("diarrhee")

facteurs_pronostiques = Int("facteurs_pronostiques")
s.add(facteurs_pronostiques >= 0)
facteurs_gravite_mineur = Int("facteurs_gravite_mineur")
s.add(facteurs_gravite_mineur >= 0)
s.add(facteurs_gravite_mineur <= 2)
facteurs_gravite_majeur = Int("facteurs_gravite_majeur")
s.add(facteurs_gravite_majeur >= 0)
s.add(facteurs_gravite_majeur <= 2)

# Contraintes définition facteurs gravité
s.add(Implies(fievre, facteurs_gravite_mineur > 0))
s.add(Implies(facteurs_gravite_mineur == 2, fievre))

variables = [age, fievre, toux, mal_de_gorge, anosmie, diarrhee,
             facteurs_pronostiques, facteurs_gravite_majeur, facteurs_gravite_mineur]

done = [False for i in range(1, 10)]
implies_done_acc = False

def implies_done(cond, number):
    global implies_done_acc
    cond_acced = And(cond, Not(implies_done_acc))
    done[number - 1] = Or(done[number - 1], cond_acced)
    implies_done_acc = Or(cond, implies_done_acc)

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


s.push()
cond = Or(done)
s.add(Not(cond))
print("Théorème: l'algorithme termine dans tous les cas")
check_and_print()
s.pop()

s.push()
for j in range(0,9):
    for i in range(0,j):
        cond = Not(And(done[i], done[j]))
        s.add(Not(cond))
print("Théorème: les conditions de sortie sont mutuellement exclusives")
check_and_print()
s.pop()
