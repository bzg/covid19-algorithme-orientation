# coding=utf8

from z3 import *

s = Solver()

age = Int("age")
fievre = Bool("fievre")
toux = Bool("toux")
mal_de_gorge = Bool("mal_de_gorge")
anosmie = Bool("anosmie")
diarrhee = Bool("diarrhee")
done = Int("fini")
facteurs_pronostiques = Int("facteurs_pronostiques")
s.add(facteurs_pronostiques >= 0)
facteurs_gravite_mineur = Int("facteurs_gravite_mineur")
s.add(facteurs_gravite_mineur >= 0)
facteurs_gravite_majeur = Int("facteurs_gravite_majeur")
s.add(facteurs_gravite_majeur >=0)

#  SI moins de 15 ans => FIN1
s.add(Implies(age <= 15, done == 1))
#
#  SI fièvre OU (toux ET mal de gorge) OU (toux ET anosmie) OU (fièvre ET diarrhée)
cond1 = Or(fievre, And(toux, mal_de_gorge), And(toux, anosmie), And(fievre, diarrhee))
#     SI 0 facteur pronostique
cond2 = (facteurs_pronostiques == 0)
#        SI 0 facteur de gravité mineur
cond3 = (facteurs_gravite_mineur == 0)
#           SI moins de 50 ans => FIN2
s.add(Implies(And(cond1, cond2, cond3, age < 50), done == 2))
#           SINON => FIN3
s.add(Implies(And(cond1, cond2, cond3, Not(age > 50)), done == 3))
#        SINON => FIN3
s.add(Implies(And(cond1, cond2), done == 3))
#
#     SI 1 OU plus facteurs pronostique
cond4 = (facteurs_pronostiques > 0)
#        SI 0 OU 1 facteur de gravité mineur => FIN3
s.add(Implies(And(cond1, cond4, Or(facteurs_gravite_mineur == 0, facteurs_gravite_mineur == 1)), done == 3))
#        SI au moins 2 facteurs de gravité mineurs => FIN4
s.add(Implies(And(cond1, cond4, facteurs_gravite_mineur > 1), done == 4))
#
#     SI un facteur de gravité majeur => FIN5
s.add(Implies(And(cond1, facteurs_gravite_majeur > 0), done == 5))
#
#  SI fièvre ET toux
cond5 = And(fievre, toux)
#     SI 0 facteur pronostique
cond6 = (facteurs_pronostiques == 0)
#        SI 0 OU 1 facteur de gravité mineurs => FIN6
s.add(Implies(And(cond5, cond6, Or(facteurs_gravite_mineur == 0, facteurs_gravite_mineur == 1)), done == 6))
#     SI 1 OU plus facteur pronostique
cond7 = (facteurs_pronostiques > 0)
#        SI 0 OU 1 facteur de gravité mineurs => FIN6
s.add(Implies(And(cond5, cond7, Or(facteurs_gravite_mineur == 0, facteurs_gravite_mineur == 1)), done == 6))
#        SI au moins 2 facteurs de gravité mineurs => FIN4
s.add(Implies(And(cond5, cond7, facteurs_gravite_mineur > 1), done == 4))
#
#  SI 1 OU plus facteur de gravité majeur => FIN5
s.add(Implies(facteurs_gravite_majeur > 0, done == 5))
#
#  SI toux OU mal de gorge OU anosmie ET pas de fièvre
cond8_alt1 = Or(toux, mal_de_gorge, And(anosmie, Not(fievre)))
cond8_alt2 = And(Or(toux, mal_de_gorge, anosmie), Not(fievre))
cond8 = cond8_alt1
#     SI 0 facteur de gravité mineur => FIN7
s.add(Implies(And(cond8, facteurs_gravite_mineur == 0), done == 7))
#     SI au moins un facteur de gravité mineur OU un facteur pronostique => FIN8
s.add(Implies(And(cond8, Or(facteurs_gravite_mineur > 0, facteurs_pronostiques > 0)), done == 8))
#
#  SI NI toux NI mal de gorge NI anosmie NI fièvre => FIN9
s.add(Implies(And(Not(toux), Not(mal_de_gorge), Not(anosmie), Not(fievre)), done == 9))

s.add(done == 0)

c = s.check()

if c == sat:
    print(s.model())
elif c == unsat:
    print("Pas de non-terminaison !")
