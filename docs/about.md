# À propos — Mnemos_Core

## Présentation
Mnemos_Core est un petit framework Java orienté vers la construction d'API Web légères basées sur des annotations. Il fournit des annotations pour déclarer des contrôleurs et des mappings d'URL, une intégration avec le contexte Spring (initialisation / gestion de beans), des utilitaires de réflexion et des composants pour exposer une API via un point d'entrée servlet. Le framework gère la résolution des routes, l'injection de paramètres simples et l'importation de fichiers via une structure légère d'annotations et d'intercepteurs.

Important : l'annotation `@Param` doit être utilisée uniquement pour les types primitifs (et leurs wrappers) ; pour l'importation de fichiers le type prévu est `MapFile` (utilisé pour passer un fichier).

## Organisation des packages (top-level)
Voici la liste des packages principaux trouvés dans le code et leur rôle global :

- com.mnemos.annotation
  - Contient les annotations publiques du framework (déclaration de contrôleurs, mappings, paramètres, marqueurs d'API).
- com.mnemos.context
  - Contient la gestion du contexte d'exécution (intégration / initialisation Spring).
- com.mnemos.exception
  - Contient les exceptions spécifiques du framework (erreurs de mapping, route non trouvée).
- com.mnemos.listener
  - Événements et listeners (lifecycle, hooks) — point d'extension pour comportements asynchrones/événements.
- com.mnemos.reflect
  - Utilitaires de réflexion pour découvrir et appeler les méthodes annotées, résoudre signatures, etc.
- com.mnemos.servlet
  - Point d'entrée HTTP (servlet) qui reçoit les requêtes et délègue au routage/mapping du framework.
- com.mnemos.utils
  - Fonctions utilitaires réutilisables (conversion, parsing, aides IO, validation légère).

## Liste des classes identifiées et rôles détaillés

Package : com.mnemos.annotation
- Controller.java
  - Rôle : annotation de niveau classe pour marquer une classe comme contrôleur de routes Web. Sert à regrouper des méthodes mappées et à indiquer au scanner du framework que la classe doit être inspectée pour y trouver des mappings d'URL.
- UrlMapping.java
  - Rôle : annotation appliquée sur des méthodes (ou éventuellement sur des classes + méthodes) pour déclarer l'URL (ou pattern) via laquelle la méthode est accessible. Contient généralement l'URL path et éventuellement la méthode HTTP (GET/POST/etc.).
- Param.java
  - Rôle : annotation appliquée sur les paramètres de méthode pour indiquer que la valeur doit être extraite depuis la requête (paramètre de query, body simple, header, etc.). À noter explicitement : `@Param` est destiné uniquement aux types primitifs / types simples (int, long, boolean, String, wrappers, etc.). Pour l'import de fichier, utiliser MapFile (voir remarque).
- WebAPI.java
  - Rôle : annotation marqueur au niveau classe pour indiquer qu'il retourne un JSON.

Package : com.mnemos.context
- SpringContext.java
  - Rôle : wrapper / adaptateur pour l'intégration avec le conteneur Spring. Responsable de :
    - initialiser le contexte Spring si présent,
    - récupérer des beans et les exposer au framework,
    - support pour injection de dépendances dans les instances contrôlées par Mnemos,
    - fournir des hooks d'initialisation et d'arrêt (lifecycle).

Package : com.mnemos.exception
- RouteMappingException.java
  - Rôle : exception levée lors d'un problème de mapping de route (par exemple mapping ambigu, signature incorrecte, paramètre manquant, type non supporté pour @Param).
- UrlNotFoundException.java
  - Rôle : exception levée quand une requête HTTP n'a pas de mapping correspondant dans le framework (404 logique).

Package : com.mnemos.listener
- (contenu non listé dans l'index fourni)
  - Rôle attendu : contiendra des interfaces et classes implémentant des listeners d'événements (initialisation, destruction, hooks avant/après exécution de handler, gestion d'événements asynchrones). Permet d'étendre le comportement du framework sans modifier le routeur principal.

## Package : com.mnemos.reflect

Ce package contient les utilitaires de réflexion Java utilisés par le framework pour inspecter les classes, récupérer leurs champs et méthodes, instancier des objets, puis invoquer dynamiquement des méthodes à l'exécution.

### Classe principale
- Reflect.java
  - Rôle : classe utilitaire de base pour la manipulation dynamique des classes Java via reflection.
  - Elle sert à :
    - vérifier que les objets passés à la réflexion ne sont pas nuls,
    - accéder aux champs et méthodes d'une classe ou d'un objet,
    - instancier une classe sans constructeur explicite,
    - rechercher les accesseurs getters/setters à partir d'un champ,
    - appeler une méthode Java de manière dynamique.

### Méthodes de la classe Reflect

#### 1) checkValue(Object o)
- Rôle : méthode interne de validation.
- Comportement :
  - vérifie si l'objet transmis est null,
  - lance une RuntimeException si l'objet est null.
- Utilité : éviter les erreurs de reflection sur un objet inexistant.

#### 2) getMethodParameter(Method method)
- Rôle : retourne les paramètres d'une méthode.
- Retour :
  - `Parameter[]`
- Utilité : permet d'inspecter la signature d'une méthode Java, notamment pour comprendre quelles données elle attend.

#### 3) getAllObjectFields(Object o)
- Rôle : récupère tous les champs déclarés de la classe de l'objet.
- Retour :
  - `Field[]`
- Comportement :
  - utilise `o.getClass().getDeclaredFields()`
  - inclut les champs privés, protégés et publics, même si eux ne sont pas accessibles directement.
- Utilité : permet au framework de parcourir les propriétés internes d'un objet et de les traiter automatiquement.

#### 4) getPublicObjectFields(Object o)
- Rôle : récupère uniquement les champs publics accessibles depuis l'extérieur.
- Retour :
  - `Field[]`
- Comportement :
  - utilise `o.getClass().getFields()`
- Utilité : utile quand le framework ne doit traiter que les attributs publics d'un objet.

#### 5) getObjectMethods(Object o)
- Rôle : retourne toutes les méthodes déclarées de la classe concernée.
- Retour :
  - `Method[]`
- Comportement :
  - si l'objet est une classe (`Class<?>`), il retourne les méthodes déclarées de cette classe,
  - sinon, il retourne les méthodes déclarées de l'instance.
- Utilité : permet de lister les opérations disponibles sur une classe pour les analyser ou les appeler dynamiquement.

#### 6) getInstance(Class<?> clazz)
- Rôle : crée une instance d'une classe via son constructeur par défaut.
- Comportement :
  - appelle `clazz.getDeclaredConstructor().newInstance()`
  - en cas d'erreur, lance une RuntimeException avec le message "Erreur d'instanciation".
- Utilité : permet d'instancier automatiquement une classe sans devoir l'initialiser manuellement.

#### 7) getMethodByName(Object o, Field field, String option)
- Rôle : recherche un getter ou un setter à partir d'un champ.
- Paramètres :
  - `o` : objet contenant le champ,
  - `field` : champ Java à analyser,
  - `option` : type de méthode à chercher, soit `"set"` soit `"get"`
- Comportement :
  - construit le nom de la méthode à partir du champ :
    - `"set" + capitalize(field.getName())`
    - `"get" + capitalize(field.getName())`
  - puis recherche la méthode précise avec `getDeclaredMethod(methodName, field.getType())`
- Exemple :
  - champ `name` → méthode `setName(String)`
  - champ `age` → méthode `getAge()`
- Utilité : permet d'associer automatiquement un champ à son accesseur correspondant.

#### 8) invoke(Method method, Object o, Object... arguments)
- Rôle : invoque dynamiquement une méthode Java.
- Comportement :
  - appelle `method.invoke(o, arguments)`
  - si une erreur survient, elle est relancée sous forme d'Exception avec un message explicite.
- Utilité : centralise l'appel de méthodes via reflection, ce qui facilite le traitement dynamique des handlers, des setters, ou des méthodes annotées.

### Rôle global du package com.mnemos.reflect
Le package `com.mnemos.reflect` est le composant du framework chargé de la manipulation dynamique des classes Java. Il intervient dans les cas suivants :
- inspection des contrôleurs et des méthodes annotées,
- lecture des champs et des méthodes d'un objet,
- instanciation automatique d'objets,
- appel dynamique des méthodes au moment de l'exécution,
- récupération de getters/setters associés à des attributs,
- binding des paramètres reçus à partir d'une requête HTTP.

En pratique, ce package est un moteur d'introspection de runtime utilisé pour rendre le framework capable de :
- découvrir les méthodes mappées,
- lire leurs paramètres,
- créer les objets nécessaires,
- appeler les méthodes sans codage manuel explicite.

### Remarque importante
Le `@Param` du framework est conçu principalement pour les types primitifs et les types simples. Pour l'import de fichiers, il faut utiliser `MapFile` plutôt que `@Param`. Cela signifie que le mécanisme de reflection est utilisé pour l'injection de valeurs simples et pour la gestion des objets spécifiques du framework, sans mélanger les responsabilités.
Package : com.mnemos.servlet
- (contenu non listé dans l'index fourni)
  - Rôle attendu : implémentation d'un HttpServlet (ou équivalent) qui :
    - reçoit la requête HTTP,
    - extrait le path et la méthode HTTP,
    - délègue au mécanisme de routage (UrlMapping) pour trouver le handler,
    - résout et injecte les paramètres annotés (via @Param ou MapFile),
    - exécute le handler et sérialise la réponse,
    - gère les exceptions du framework (RouteMappingException -> 500, UrlNotFoundException -> 404, etc.).

Package : com.mnemos.utils
- (contenu non listé dans l'index fourni)
  - Rôle attendu : fonctions utilitaires communes : conversion de types (String → int/boolean/date), gestion de MapFile, helpers IO, helpers pour la construction de réponses, aids pour logging léger.

Autres éléments à la racine
- pom.xml
  - Rôle : configuration Maven du projet (dépendances, plugins, packaging).
- compilation.txt / todo.md
  - Notes et TODOs du projet.
- framework-bin, lib
  - Répertoires d'artefacts livrables (binaire ou jar packagé).

## Flux d'exécution (vue d'ensemble, requête → réponse)
1. Le serveur / conteneur web envoie toutes les requêtes HTTP au servlet principal fourni dans com.mnemos.servlet.
2. Le servlet extrait le chemin de la requête et la méthode HTTP, puis appelle le composant de routage.
3. Le scanner/reflection (com.mnemos.reflect) a préalablement construit une table de routage en parcourant toutes les classes annotées `@Controller` et `@WebAPI`, lisant les `@UrlMapping` et enregistrant les méthodes handlers.
4. Le routeur recherche la méthode correspondante :
   - Si aucune méthode trouvée → lève UrlNotFoundException (404).
   - Si plusieurs méthodes correspondent ou signature invalide → RouteMappingException.
5. Pour la méthode choisie, le résolveur de paramètres instancie les arguments :
   - Pour chaque paramètre annoté `@Param` (primitif) : convertir la valeur de la requête au type attendu.
   - Pour l'import de fichiers : passer une instance de `MapFile` (gestion de l'upload/stream).
6. Le framework invoque la méthode handler (utilisant SpringContext pour résoudre/injecter des beans si nécessaire) et récupère un objet résultat.
7. Le résultat est sérialisé en réponse HTTP (JSON/Text/etc.) par une utilité du package utils ou par un adaptateur de sérialisation.
8. Les exceptions sont capturées et transformées en codes HTTP appropriés (404, 500, 400 selon le cas), avec logs éventuels.

## Règles et conventions importantes
- @Param : réservé aux types primitifs / wrappers / String. N'utiliser @Param pour des objets complexes ; pour ces derniers, prévoir un mécanisme de binding dédié (non fourni par @Param).
- MapFile : utilisé pour indiquer l'importation d'un fichier dans un paramètre de méthode (upload). Le framework doit fournir la conversion de la part multipart/form-data vers MapFile.
- Les classes annotées `@Controller` et `@WebAPI` sont automatiquement scannées au démarrage (ou au premier accès) ; pour intégrer Spring, utiliser SpringContext afin de récupérer et injecter des beans.
- Les exceptions spécifiques du framework (RouteMappingException, UrlNotFoundException) servent à centraliser le traitement d'erreur pour produire des réponses HTTP cohérentes.


---
Si tu veux que je crée automatiquement ce fichier docs/about.md dans le dépôt, je peux le faire (je mettrai exactement ce contenu dans docs/about.md). Veux-tu que je le commette maintenant ?
