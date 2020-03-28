# coding=utf8

from z3 import *

# D'abord, il faut déclarer créer une instance du solveur Z3
s = Solver()

# On déclare toutes les entrées du problèmes, booléennes ou entières
age = Int("age")
fievre = Bool("fievre")
toux = Bool("toux")
douleurs = Bool("douleurs")
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
variables = [age, fievre, toux, douleurs, anosmie, diarrhee,
             facteurs_pronostiques, facteurs_gravite_majeur, facteurs_gravite_mineur]


# La fonction suivante nous permet d'encoder comme une contrainte le fait
# de déclarer une condition de terminaison de l'algorithme.
# Ici "cond" est la condition à laquelle on veut que l'algorithme termine, et
# "number" le numéro du cas de terminaison.
def implies_done_base(cond, number, done, implies_done_acc):
    # L'algorithme est séquentiel : lorsque l'on atteint une condition de
    # terminaison, on arrête de l'exécuter. Or Z3 n'est pas séquentiel, les
    # contraintes ne fonctionnent pas comme ça. On a donc besoin ici d'un
    # accumulateur des conditions de terminaisons déjà rencontrées afin
    # d'émuler ce comportement séquentiel pour Z3.
    # On termine dans ce cas si la condition est remplie et que l'on est pas
    # dans un des cas de terminaison précédent.
    cond_acced = And(cond, Not(implies_done_acc))
    # On peut définit plusieurs conditions qui mènent au même cas de terminaison,
    # il est donc nécessaire de faire un "ou" logique avec le contenu de la
    # case de la liste.
    done[number - 1] = Or(done[number - 1], cond_acced)
    # On met à jour l'accumulateur
    return Or(cond, implies_done_acc)

# Encodage du pseudo-code dans Z3

# VERSION b782eb0a6a1c - samedi 28 mars 15 h
def algo_b782eb0a6a1c():
    # L'accumulateur est initialisé à False car c'est une série de ou logiques.
    implies_done_acc = BoolVal(False)
    # L'algotithme définit 10 cas de terminaison que l'on stockera dans cette liste
    # "done"
    done = [BoolVal(False) for i in range(1, 10)]

    def implies_done(cond, number):
        nonlocal implies_done_acc
        implies_done_acc = implies_done_base(
            cond, number, done, implies_done_acc)

    #  SI < 15 ans => FIN1
    implies_done(age < 15, 1)
    #
    #  SI >=1 facteur de gravité majeur => FIN5
    implies_done(facteurs_gravite_majeur >= 1, 5)
    #
    #  SI fièvre
    cond1 = fievre
    #     SI toux
    cond2 = toux
    #        SI >=1 facteur pronostique ET >=2 facteurs de gravité mineurs => FIN4
    cond3 = And(facteurs_pronostiques >= 1, facteurs_gravite_mineur >= 2)
    implies_done(And(cond1, cond2, cond3), 4)
    #        SINON => FIN6
    implies_done(And(cond1, cond2, Not(cond3)), 6)
    #     SI diarrhée OU douleurs OU anosmie
    cond4 = Or(diarrhee, douleurs, anosmie)
    #        SI >=1 facteur pronostique ET >=2 facteurs de gravité mineurs => FIN4
    implies_done(And(cond1, cond4, cond3),4)
    #        SI >= 50 ans OU >=1 facteur de gravité mineur => FIN3
    cond5 = Or(age >= 50, facteurs_gravite_mineur >= 1)
    implies_done(And(cond1, cond4, cond5), 3)
    #        SINON => FIN2
    implies_done(And(cond1, cond4, Not(cond5)), 2)
    #
    #  SI toux OU douleurs OU anosmie
    cond6 = Or(toux, douleurs, anosmie)
    #     SI >=1 facteur de gravité mineur OU >=1 facteur pronostique => FIN8
    cond7 = Or(facteurs_gravite_mineur >=1, facteurs_pronostiques >= 1)
    implies_done(And(cond6, cond7), 8)
    #     SINON => FIN7
    implies_done(And(cond6, Not(cond7)), 7)
    #
    #  SINON => FIN9
    implies_done(BoolVal(True), 9)
    return done


# VERSION 7c34d5814273 - samedi 28 mars 13 h 15
def algo_7c34d581427():
    # L'accumulateur est initialisé à False car c'est une série de ou logiques.
    implies_done_acc = BoolVal(False)
    # L'algotithme définit 10 cas de terminaison que l'on stockera dans cette liste
    # "done"
    done = [BoolVal(False) for i in range(1, 10)]

    def implies_done(cond, number):
        nonlocal implies_done_acc
        implies_done_acc = implies_done_base(
            cond, number, done, implies_done_acc)

    #  SI < 15 ans => FIN1
    implies_done(age <= 15, 1)
    #
    #  SI >=1 facteur de gravité majeur => FIN5
    implies_done(facteurs_gravite_majeur >= 1, 5)
    #
    #  SI fièvre ET toux
    cond1 = And(fievre, toux)
    #     SI >=1 facteur pronostique et >=2 facteurs de gravité mineurs => FIN4
    implies_done(And(cond1, facteurs_pronostiques >=
                     1, facteurs_gravite_mineur >= 2), 4)
    #     SINON => FIN6
    implies_done(And(cond1, Not(
        And(facteurs_pronostiques >= 1, facteurs_gravite_mineur >= 2))), 6)
    #
    #  SI fièvre OU diarrhée OU (toux ET douleurs) OU (toux ET anosmie)
    cond2 = Or(fievre, diarrhee, And(toux, douleurs), And(toux, anosmie))
    #     SI >=1 facteur pronostique
    cond3 = (facteurs_pronostiques >= 1)
    #        SI >=2 facteurs de gravité mineurs => FIN4
    implies_done(And(cond2, cond3, facteurs_gravite_mineur >= 2), 4)
    #        SINON => FIN3
    implies_done(And(cond2, cond3, facteurs_gravite_mineur < 2), 3)
    #     SINON
    #        SI >= 50 ans ou >=1 facteur de gravité mineur => FIN3
    cond4 = Or(age >= 50, facteurs_gravite_mineur >= 1)
    implies_done(And(cond2, Not(cond3), cond4), 3)
    #        SINON => FIN2
    implies_done(And(cond2, Not(cond3), Not(cond4)), 2)
    #
    #  SI pas de fièvre ET (toux OU douleurs OU anosmie)
    cond5 = And(Not(fievre), Or(toux, douleurs, anosmie))
    #     SI >=1 facteur de gravité mineur OU >=1 facteur pronostique => FIN8
    cond6 = Or(facteurs_gravite_mineur >= 1, facteurs_pronostiques >= 1)
    implies_done(And(cond5, cond6), 8)
    #     SINON => FIN7
    implies_done(And(cond5, Not(cond6)), 7)
    #
    #  SI NI toux NI douleurs NI anosmie => FIN9
    implies_done(And(Not(toux), Not(douleurs), Not(anosmie)), 9)
    return done

# VERSION bba3d6b27c5 - jeudi 26 mars 14 h 00
def algo_bba3d6b27c5():
    implies_done_acc = BoolVal(False)
    done = [BoolVal(False) for i in range(1, 10)]

    def implies_done(cond, number):
        nonlocal implies_done_acc
        implies_done_acc = implies_done_base(
            cond, number, done, implies_done_acc)

    #  SI moins de 15 ans => FIN1
    implies_done(age <= 15, 1)
    #
    #  SI (fièvre ET PAS toux) OU (toux ET mal de gorge) OU (toux ET anosmie) OU (fièvre ET diarrhée)
    cond1 = Or(And(fievre, Not(toux)), And(toux, douleurs), And(
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
    cond8_alt1 = Or(toux, douleurs, And(anosmie, Not(fievre)))
    cond8_alt2 = And(Or(toux, douleurs, anosmie), Not(fievre))
    cond8 = cond8_alt1
    #     SI au moins un facteur de gravité mineur OU un facteur pronostique => FIN8
    implies_done(
        And(cond8, Or(facteurs_gravite_mineur > 0, facteurs_pronostiques > 0)), 8)
    #     SI 0 facteur de gravité mineur => FIN7
    implies_done(And(cond8, facteurs_gravite_mineur == 0), 7)
    #
    #  SI NI toux NI mal de gorge NI anosmie NI fièvre => FIN9
    implies_done(And(Not(toux), Not(douleurs),
                     Not(anosmie), Not(fievre)), 9)
    return done

# Maintenant que l'algorithme est encodé dans Z3, on peut essayer de prouver
# les théorèmes. Cette fonction wrappe la fonction "check" de Z3 avec une
# interface qui affiche la valeur des variables du contre-exemple si le théorème
# que l'on essaye de prouver est faux.


def check_and_print(done_arrays, msg_unsat, msg_sat):
    c = s.check()
    if c == sat:
        print(msg_sat)
        print("  Valeur des variables :")
        variables_evaluated = ["    {}: {}".format(
            v, s.model().evaluate(v, model_completion=True)) for v in variables]
        print(*variables_evaluated, sep="\n")
        print("  Sorties activées :")
        for (done, msg) in done_arrays:
            done_evaluated = [("    FIN{} ({})".format(
                i + 1, msg), s.model().evaluate(d, model_completion=True)) for (i, d) in enumerate(done)]
            done_evaluated = [a for (a, b) in done_evaluated if b]
            print(*done_evaluated, sep="\n")
        print("")
    elif c == unsat:
        print(msg_unsat, "\n")


done1 = algo_7c34d581427()
done1_msg = "samedi 28 mars 13h15"
done2 = algo_bba3d6b27c5()
done2_msg = "jeudi 26 mars 14h"
done3 = algo_b782eb0a6a1c()
done3_msg = "samedi 28 mars 15h"


def check_all_cases_ending(done, msg):
    print(">>> Théorème: l'algorithme {} termine dans tous les cas".format(msg))
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
    check_and_print([(done, msg)], "OK !", "Trouvé un cas de non-terminaison !")
    s.pop()


check_all_cases_ending(done1, done1_msg)
check_all_cases_ending(done2, done2_msg)
check_all_cases_ending(done3, done3_msg)

def check_every_exit_reached(done, msg):
    print("Théorème: pour l'algorithme {} chaque sortie est atteignable".format(msg))
    for i in range(0, 9):
        print("Atteindre la sortie FIN{} ?".format(i + 1))
        s.push()
        cond = done[i]
        s.add(cond)
        check_and_print([(done, msg)], "Sortie jamais atteinte !", "Valeur d'atteinte:")
        s.pop()

check_every_exit_reached(done3, done3_msg)

def check_same(done1, msg1, done2, msg2):
    print(">>> Théorème: deux algorithmes ({} et {}) donnent les mêmes réponses".format(
        msg1, msg2))
    for i in range(0, 9):
        print("Analyse de la sortie FIN{}:".format(i + 1))
        s.push()
        cond = Xor(done1[i], done2[i])
        s.add(cond)
        check_and_print([(done1, msg1), (done2, msg2)],
                        "Les deux algorithmes sont toujours d'accord sur cette sortie !", "Trouvé une valeur de discorde:")
        s.pop()
    print("")


check_same(done1, done1_msg, done2, done2_msg)
check_same(done3, done3_msg, done2, done2_msg)
check_same(done1, done1_msg, done3, done3_msg)
