---
title: M�thodes Approch�es pour la R�solution de Probl�mes d'Ordonnancement
subtitle: Partie 1
author: Arthur Bit-Monnot, Marie-Jo Huguet
geometry: margin=3cm
...

# �tapes de mise en place

## Discord

 - rejoignez le serveur Discord pour ces TP : [https://discord.gg/KyUbCCT](https://discord.gg/KyUbCCT). 
 - indiquez votre *Pr�nom Nom* comme pseudo, pour que l'on puisse vous identifier
 - en cas de probl�me technique, vous pourrez vous adresser au chan *support-technique* de ce serveur.
 
## Document de suivi 
     
 - inscrivez vous dans le document de suivi : [https://docs.google.com/spreadsheets/d/1QAZlWaTCvrMlLLuwuVFmCD8Plr9QvOoPVpIR4g1PSBk/edit?usp=sharing](https://docs.google.com/spreadsheets/d/1QAZlWaTCvrMlLLuwuVFmCD8Plr9QvOoPVpIR4g1PSBk/edit?usp=sharing)
 - � chaque �tape franchie dans le TP, ajountez un `X` dans la case correspondante 
 
## R�cuperation du code

- R�cup�rez la base de code sur Github : [https://github.com/insa-4ir-meta-heuristiques/template-jobshop](https://github.com/insa-4ir-meta-heuristiques/template-jobshop)

- Suivez les instructions dans le README pour vous assurer que le code compile.
- Importez le projet dans votre IDE pr�f�r�. Tous doivent pouvoir supporter l'import de projet gradle et quelques liens sont donn�s en bas du README. 

 
# Prise en main

## Repr�sentation d'un probl�me de JobShop

Vous trouverez dans le dossier `instances/` un ensemble d'instances commun�ment utilis�es pour le probl�me de jobshop.
Si l'on consid�re l'instance vu dans les exercices, constitu�e de deux jobs avec trois t�ches chacun :

\begin{table}[h!]
 	\centering
 		\begin{tabular}{c | c | c | c   }
 		  $J_1$  & $r_1, 3$ & $r_2, 3$ & $r_3, 2$ \\
 		  \hline
 		  $J_2$  & $r_2, 2$ & $r_1, 2$ & $r_3, 4$ \\
 		\end{tabular}
 	\label{tab:tache}
 \end{table}
 
L'instance est nomm�e `aaa1` d�crite ci-dessus est donn�e dans le fichier `instances/aaa1` :

```
    # Fichier instances/aaa1
    2 3 # 2 jobs and 3 tasks per job
    0 3 1 3 2 2 # Job 1 : (machine duration) for each task 
    1 2 0 2 2 4 # Job 2 : (machine duration) for each task 
```

La premi�re ligne donn�e le nombre de jobs et le nombre de t�ches par jobs. Le nombre de machine est �gal au nombre de t�ches.
Chacune des lignes suivantes sp�cifie la machine (ici num�rot�es `0`, `1` et `2`) et la dur�e de chaque t�che. Par exemple la ligne
`0 3 1 3 2 2` sp�cifie que pour le premier job :

 - `0 3`: la premi�re t�che s'execute sur la machine `0` et dure `3` unit�s de temps
 - `1 3`: la deuxi�me t�che s'execute sur la machine `1` et dure `3` unit�s de temps
 - `2 2`: la troisi�me t�che s'execute sur la machine `2` et dure `2` unit�s de temps

 
## Base de code

Il vous est fourni une base de code pour faciliter votre prise en main du probl�me.
Vous trouverez dans la classe `jobshop.Main` un point d'entr�e pour r�aliser des tests de performance de m�thodes heuristiques.
Les autres points d'entr�e du programme sont les tests d�j� pr�sent ou pouvant �tre ajout�s dans le dossier `src/test/java/`. 

### Probl�me et Solution
 
 - classe `jobshop.Instance` qui contient la repr�sentation d'un probl�me et une m�thode pour parser un fichier de probl�me.
 - classe `jobshop.Schedule` qui contient la repr�sentation directe, associant � chaque t�che une date de d�but.
 La classe schedule sert notamment � la repr�sentation d'une solution et toute autre repr�sentation doit pouvoir �tre traduite dans un `Schedule`
 
### Repr�sentation 
 
 - une classe abstraite `jobshop.Encoding` dont les sous classes sont des repr�sentations d'une solution au JobShop. 
 - une classe `jobshop.encoding.NumJobs` qui contient une impl�mentation de la repr�sentation par num�ro de job
 
### Solveurs

Deux solveurs tr�s simples bas�s sur une repr�sentation par num�ro de job. Les nouveaux solveurs doivent �tre ajout�s � la structure `jobshop.Main.solvers` pour �tre accessibles dans le programme principal.
 
 - `basic` : m�thode pour la g�n�ration d'une solution pour une repr�sentation par num�ro de job
 - `random` : m�thode de g�n�ration de solutions al�atoires par num�ro de job



## � faire : manipulation de repr�sentations

Ouvrez la m�thode `DebuggingMain.main()`. 

 - Pour la solutions en repr�sentation par num�ro de job donn�e, calculez (� la main) les dates de d�but de chaque t�che
 - impl�mentez la m�thode `toString()` de la classe `Schedule` pour afficher les dates de d�but de chaque t�che dans un schedule.
 - V�rifiez que ceci correspond bien aux calculs que vous aviez fait � la main.
 
Cr�ation d'une nouvelle representation par ordre de passage sur les ressources : 

- Cr�er une classe `jobshop.encodings.ResourceOrder` qui contient la repr�sentation par ordre de passage sur ressources vue dans les exercices (section 3.2). Il s'agit ici d'une reprsentation sous forme de matrice o� chaque ligne correspond � une machine, et sur cette ligne se trouvent les t�ches qui s'ex�cutent dessus dans leur ordre de passage. Pour la representation d'une t�che dans la matrice, vous pouvez utiliser la classe `jobshop.encodings.Task` qui vous est fournie.

- Pour cette classe, impl�mentez la m�thode `toSchedule()` qui permet d'extraire une repr�sentation directe. 
Pour l'impl�mentation de cette m�thode `toSchedule()`, il vous faut construire un schedule qui associe � chaque t�che une date de d�but (vous pouvez regardez l'impl�mentation pour `JobNums` pour en comprendre le principe).
Pour construire ce schedule il faudra que vous mainteniez une liste des t�ches qui ont �t� sch�dul�. Cette liste est initialement vide.
� chaque it�ration de l'algorithme, on identifie les t�ches executables. Une t�che est executable si 

     - son pr�decesseur sur le job a �t� schedul� (si c'est la tache (1, 3), il faut que les t�ches (1,1) et (1,2) aient �t� sch�dul�e)
     - son pr�decesseur sur la ressource a �t� schedul� (l'ordre sur passage sur les ressources est pr�cisement ce qui vous est donn� par cette repr�sentation).
 
- Ajouter des tests dans `src/test/java/jobshop` permettant de v�rifier que vos m�thodes fonctionnent bien pour les exemples trait�s en cours (instance `aaa1`). Vous pouvez pour cela vous inspirer et ajouter des cas de test � `EncodingTests`. 



Changement de repr�sentation : 

- pour les deux repr�sentations `ResourceOrder` et `JobNums`, cr�ez des m�thodes permettant de cr�er cette repr�sentation depuis un `Schedule`.
- utilisez la pour tester la conversion de `ResourceOrder` vers `JobNums` et vice-versa.


# Heuristiques gloutonne

Un sch�ma g�n�ral d'heuristique est pr�sent� dans l'article [@Blazewicz1996] et est r�sum� ci-dessous:

 #. se placer � une date $t$ �gale � la plus petite date de d�but des op�rations 
 #. construire l'ensemble des op�rations pouvant �tre r�alis�es � la date $t$
 #. s�lectionner l'operation $(i,j)$ r�alisable de plus grande priorit�
 #. placer $(i,j)$ au plus t�t sur la ressource $k$ qui la r�alise (en respectant les contraintes de partage de ressources, c'est � dire en v�rifiant la date de disponibilit� de la ressource $k$)
 #. recommencer en (3) tant qu'il reste des op�rations � ordonnancer 
 #. recommencer en (2) en incr�mentant la date $t$ (� la prochaine date possible compte tenu des dates de d�but des op�rations)


Selon la mani�re dont sont g�r�es les priorit�s entre les op�rations on obtient diff�rentes solutions d'ordonnancement. Les r�gles de piorit� classiquement utilis�es sont : 

 - SPT (Shortest Processing Time) : donne la priorit� � la t�che ayant la plus petite dur�e;
 - LPT (Longest Processing Time) : donne la priorit� � la t�che ayant la plus grande dur�e 

## � faire

 - Cr�er un nouveau solveur impl�mentant une recherche gloutonne pour les priorit�s SPT et LPT, bas� sur la repr�sentation par ordre de passage sur les ressources
 - Evaluer ces heuristiques sur les instances fournies et pour la m�trique d'�cart fournie.
 - (optionnel) Concevoir une version randomis�e de ces heuristiques o� une partie des choix est soumise � un tirage al�atoire
 - D�buter la r�daction d'un rapport pr�sentant le travail effectu�

Pour les tests de performance, on privil�giera les instance `ft06`, `ft10`, `ft20` et les instances de Lawrence `la01` � `la40`.

<!--
 - Concevoir version *randomis�e* de ces recherches gloutonnes
 - Evaluer cette nouvelle heuristique sur les instances fournies et pour les m�triques d�finies pr�c�demment
 - Comparer les r�sultats obtenus par ces deux heuristiques par rapport � ceux de la litt�rature et � la m�thode exacte
 - D�buter la r�daction d'un rapport pr�sentant le travail effectu�
-->

# Recherche exhaustive

Dans le but de garantir l'optimalit� des solutions trouv�e, impl�mentez une recherche exhaustive bas�e sur une recherche en profondeur d'abord (Depth-First Search).  
On notera que par rapport � la m�thode gloutonne, une recherche exhaustive doit permettre de tester l'ensemble des op�rations pouvant �tre r�alis�e � instant $t$ (�tape (2) de la m�thode gloutonne ci-dessus).
On ne cherchera pas � optimiser cette m�thode, des techniques plus adapt�es � la recherche exhaustive pour de tels probl�mes seront trait�es en 5�me ann�e.

Quelle semble �tre la taille limite d'une instance pour permettre une recherche exhaustive ?

# R�f�rences 

<!--
# M�thode de descente

- impl�menter chemin critique
- m�thode de validation

- impl�menter voisinage 
   - Laarhoven
   - Nowicki et Smutnicki (optionnel)

- m�thode descente 

- m�thode de descente avec d�part multiples (optionnel)


# M�taheuristiques

Au choix : M�thode tabou ou Algorithme g�n�tique



 -->
 
 
 