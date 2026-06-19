## Instruction Sprint 0:
- Création d'un servlet: FrontControllerServlet
- doGet
- doPost
- processRequest(appelé par doGet et doPost)

**Objectif:**
- Afficher l'url par le processRequest
- Pour le test:
<<<<<<< Updated upstream
* configurer le web.xml du test afin d'initialiser le servlet et aussi les request param
=======
* configurer le web.xml du test afin d'initialiser le servlet et aussi les request param

## Instruction Sprint 1:
```
Prérequis
- Création d'une annotation(champ d'application: classe, methode, attribut)
```
- Créer une annotion Controller pour une classe controller dans un package annotion
- Créer un code qui sera appeler soit 
 - **au démarrage de l'application web**:
    * Utilisant un listener 
    * Ce code liste toutes les classes controller dans le classPath du projet test
  - **Soit executer au première appel de FrontServlet**:
    * dans init 
 Dans FrontServlet on ajoute un attribut List<String> listController qui sera remplis dans la méthode init
  - Pour annotation.Controller: soit on scan toutes les packages, soit on lui donne juste la liste des packages à scanner  
  - Il faut alors ajouter le package qui contient les controllers dans web.xml puis dans init on récupère cette variable pour le scanner dans init
  - Il faut une méthode géneraliser qui prend comme paramètre(annotation, package, niveau(au niveau classe, attribut, constructeur))

## Instruction Sprint 2:
- Créer une annotation pour une méthode urlMapping
  - On met dans le paramètre de cette annotation de l'url
- On prend tous les controllers et afficher
  - url-classe-méthode (à stocker quelque par dans FrontServlet)
>>>>>>> Stashed changes
