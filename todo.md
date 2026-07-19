# Instruction Sprint 0:
- Création d'un servlet: FrontControllerServlet
- doGet
- doPost
- processRequest(appelé par doGet et doPost)

**Objectif:**
- Afficher l'url par le processRequest
- Pour le test:
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
- Créer une annotation pour une méthode : urlMapping avec attribut url
- affichena ny liste url-classe-méthode misy anazy
- throws Exception(liste url supporté) si l'url saisie n'est pas valide

## Instruction Sprint 3:
- Création classe UrlMethod(url, method="GET"/"POST")
- Ajout attribut method pour l'annotation
- Redifinir la fonction equals pour UrlMethod pour éviter les doublons

## Instruction Sprint 3:
- Executer la méthode associé au lien

## Instruction Sprint 4:
- Intégration du ContextListener
- Optmisation scanne du package

## Instruction Sprint 5:
- Passer des données dans un view(comment ?)
    * Ajouter des attributs prefix et suffix dans le web.xml pour le prefix et suffix du view
    * Création d'une classe ModelAndView:
        - Attribut:
            * Map<String, Object> attributes
            * url(pour le view) 
        - méthode setAttribut(String, Object)
        - méthode setUrl(String)
    * La méthode du controller doit retourner un ModelAndView 
    * On invoque la méthode du controller et on récupère le ModelAndView pour ajouter les attributs dans la request et faire un forward vers le view

## Sprint-5bis
- Intégrer base de données
- Combiner le frameword avec les couches services,repository de Spring
- **Réflection**:
  - Comment gérer la cycle de vie de Spring et du Framework
  - COmment éviter l'instanciation d'un conteneur à chaque appel du controller
  - Le programme doit être un singleton au lieu d'un prototype
  - Il faut prendre le contexte de l'application Spring, en l'initialisant dans un listener
  - Passer le contexte comme argument d'une méthode à invoquer
  - **à rechercher**:
    - comment instancier le contexte du spring
    - ajouter du listener dans le web.xml