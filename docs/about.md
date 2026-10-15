# À propos — Mnemos_Core

## Présentation
Mnemos_Core est un framework Java pour applications Web reposant sur des annotations. Il fournit des annotations de contrôleur et de route, l'intégration avec le contexte Spring, des utilitaires de réflexion et un servlet frontal. Il gère le routage HTTP, le binding des arguments des handlers, les réponses JSON et le transfert vers des vues.

Important : `@Param` identifie le nom du paramètre de requête. Les valeurs simples sont converties selon les types pris en charge par `Utilitaire.dynamicCast` ; les imports de fichiers utilisent `MapFile` comme type de paramètre annoté.

## Organisation des packages (top-level)
Voici la liste des packages principaux trouvés dans le code et leur rôle global :

- com.mnemos.annotation
  - Contient les annotations runtime utilisées pour déclarer les contrôleurs, les routes HTTP, les paramètres de requête et les handlers qui renvoient du JSON.
- com.mnemos.context
  - Fournit un accès au contexte Spring et à ses beans.
- com.mnemos.exception
  - Contient les exceptions dédiées aux erreurs de mapping et aux routes introuvables.
- com.mnemos.listener
  - Initialise les routes et les objets de contexte au démarrage de l'application web.
- com.mnemos.reflect
  - Utilitaires de réflexion pour découvrir et appeler les méthodes annotées, résoudre signatures, etc.
- com.mnemos.servlet
  - Reçoit les requêtes HTTP, résout la route, lie les arguments et traite la réponse.
- com.mnemos.utils
  - Fournit le scan des contrôleurs, le routage, le binding des paramètres et des utilitaires de conversion et de réponse.

## Liste des classes identifiées et rôles détaillés

Package : com.mnemos.annotation
- Controller.java
  - Rôle : annotation runtime de type (`@Target(TYPE)`) qui marque une classe comme contrôleur.
  - Utilisation : le listener demande au scanner de ne retenir que les classes portant cette annotation, puis inspecte leurs méthodes mappées.
- UrlMapping.java
  - Rôle : annotation runtime de méthode (`@Target(METHOD)`) qui associe un handler à une URL et à une méthode HTTP.
  - Attributs : `url` vaut `/` par défaut ; `method` vaut `GET` par défaut. La valeur de `method` est comparée sans tenir compte de la casse par `UrlMethod`.
- Param.java
  - Rôle : annotation runtime de paramètre (`@Target(PARAMETER)`) dont l'attribut obligatoire `value` donne le nom du paramètre de requête.
  - Utilisation : le binder l'emploie pour lire une valeur de formulaire/requête et la convertir au type déclaré. Elle peut aussi nommer un paramètre `MapFile` ou une liste.
- WebAPI.java
  - Rôle : annotation runtime de méthode (`@Target(METHOD)`) qui indique que le résultat du handler doit être traité comme une réponse JSON.
  - Le servlet sérialise le résultat et choisit le type de contenu en fonction du contenu produit.

Package : com.mnemos.context
- SpringContext.java
  - Rôle : petit adaptateur qui conserve une référence à `ApplicationContext`.
  - `SpringContext(ApplicationContext)` et `setContext(ApplicationContext)` définissent le contexte ; `getContext()` le retourne.
  - `getBean(Class<T>)` récupère un bean par son type. Si le contexte n'est pas défini, la méthode lève une `RuntimeException`.
  - Le listener lui fournit le `WebApplicationContext` du conteneur web. `Utilitaire.invoke` s'en sert pour injecter les champs correspondant à des types annotés `@Service`.

Package : com.mnemos.exception
- RouteMappingException.java
  - Rôle : exception runtime dédiée aux erreurs de mapping de route. Elle prend un message en argument. Le code actuellement consulté ne la lève pas directement.
- UrlNotFoundException.java
  - Rôle : exception runtime levée par `Utilitaire.getByUrlMethod` quand aucune route ne correspond à l'URL et à la méthode HTTP.

Package : com.mnemos.listener
- AppStartListener.java
  - Rôle : listener de cycle de vie Servlet déclaré avec `@WebListener`.
  - À l'initialisation du contexte web, récupère le `WebApplicationContext` Spring, crée `SpringContext`, lit les paramètres de contexte `packageController`, `prefix` et `suffix`, puis construit la table des routes à partir des classes `@Controller` et des méthodes `@UrlMapping`.
  - Publie `routes`, `prefix`, `suffix` et `springContext` dans le `ServletContext` pour le servlet frontal.
  - Les erreurs pendant le scan sont propagées sous forme de `RuntimeException`. `contextDestroyed` n'effectue actuellement aucun traitement.

#### Package : com.mnemos.reflect

Ce package contient les utilitaires de réflexion Java utilisés par le framework pour inspecter les classes, récupérer leurs champs et méthodes, instancier des objets, puis invoquer dynamiquement des méthodes à l'exécution.

##### Classe principale
- Reflect.java
  - Rôle : classe utilitaire de base pour la manipulation dynamique des classes Java via reflection.
  - Elle sert à :
    - vérifier que les objets passés à la réflexion ne sont pas nuls,
    - accéder aux champs et méthodes d'une classe ou d'un objet,
    - instancier une classe sans constructeur explicite,
    - rechercher les accesseurs getters/setters à partir d'un champ,
    - appeler une méthode Java de manière dynamique.

##### Méthodes de la classe Reflect

###### 1) getMethodParameter(Method method)
- Rôle : retourne les paramètres d'une méthode.
- Retour :
  - `Parameter[]`
- Vérifie d'abord que `method` n'est pas `null`.

###### 2) getAllObjectFields(Object o)
- Rôle : récupère tous les champs déclarés de la classe de l'objet.
- Retour :
  - `Field[]`
- Comportement :
  - utilise `o.getClass().getDeclaredFields()`
  - inclut les champs privés, protégés et publics, même si eux ne sont pas accessibles directement.
- Utilité : permet au framework de parcourir les propriétés déclarées de l'objet pour le binding et l'injection.

###### 3) getDeclaredField(Object o, String name)
- Rôle : recherche un champ déclaré portant le nom demandé sur la classe de l'objet.
- Retour : `Field`.
- Si le champ n'existe pas, lève une `RuntimeException`.

###### 4) getPublicObjectFields(Object o)
- Rôle : récupère uniquement les champs publics accessibles depuis l'extérieur.
- Retour :
  - `Field[]`
- Comportement :
  - utilise `o.getClass().getFields()`
- Utilité : expose aussi les champs publics hérités.

###### 5) getObjectMethods(Object o)
- Rôle : retourne toutes les méthodes déclarées de la classe concernée.
- Retour :
  - `Method[]`
- Comportement :
  - si l'objet est une classe (`Class<?>`), il retourne les méthodes déclarées de cette classe,
  - sinon, il retourne les méthodes déclarées de l'instance.
- Les méthodes héritées ne sont pas incluses lorsque la classe est interrogée.

###### 6) getInstance(Class<?> clazz)
- Rôle : crée une instance d'une classe via son constructeur par défaut.
- Comportement :
  - appelle `clazz.getDeclaredConstructor().newInstance()`
  - en cas d'erreur, lance une RuntimeException avec le message "Erreur d'instanciation".
- Utilité : permet d'instancier automatiquement une classe sans devoir l'initialiser manuellement.

###### 7) getMethodByName(Object o, String name, String option, Class<?>... parameterType)
- Rôle : recherche une méthode publique par nom et types de paramètres ; peut aussi construire le nom d'un getter ou setter à partir d'un nom de propriété.
- Paramètres :
  - `o` : objet contenant le champ,
  - `name` : nom de la méthode ou de la propriété,
  - `option` : chaîne vide pour le nom littéral, `"set"` ou `"get"` pour construire `set<Name>` ou `get<Name>`,
  - `parameterType` : types exacts des paramètres attendus (par exemple le type du champ pour un setter).
- Comportement :
  - cherche la méthode avec `getMethod`, puis la rend accessible.
  - lève une `RuntimeException` si aucune méthode correspondante n'est trouvée ou si `option` est invalide.

###### 8) invoke(Method method, Object o, Object... arguments)
- Rôle : invoque dynamiquement une méthode Java.
- Comportement :
  - appelle `method.invoke(o, arguments)`
  - si une erreur survient, elle est relancée sous forme d'Exception avec un message explicite.
- Utilité : centralise l'appel de méthodes via reflection, ce qui facilite le traitement dynamique des handlers, des setters, ou des méthodes annotées.

###### 9) getListElementType(Parameter p), getListType(Parameter p)
- `getListType` retourne le type générique déclaré du paramètre.
- `getListElementType` vérifie que le paramètre est une `List` paramétrée avec un seul argument de type concret, puis retourne la classe de cet élément. Les types génériques non résolus ne sont pas pris en charge.

##### Rôle global du package com.mnemos.reflect
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
`@Param` sert à nommer les valeurs issues de la requête, y compris pour un paramètre `MapFile`. Les objets métier peuvent aussi être liés sans `@Param` : le binder les instancie et renseigne leurs champs depuis les paramètres de requête.
Package : com.mnemos.servlet
- FrontControllerServlet.java
  - Rôle : servlet frontal configuré pour le multipart (`@MultipartConfig`), qui traite les requêtes `GET` et `POST`.
  - À l'initialisation, récupère du `ServletContext` les attributs `routes`, `prefix`, `suffix` et `springContext`, puis crée le binder des données.
  - Pour chaque requête, retire le chemin de contexte, cherche la route correspondante à partir de l'URL et de la méthode HTTP, construit les arguments avec `DataBinder`, puis invoque le handler.
  - Si le handler renvoie un `ModelAndView`, copie ses attributs dans la requête et transmet le traitement à la vue construite comme `prefix + url + suffix`.
  - Si la méthode porte `@WebAPI`, sérialise le résultat et l'envoie dans la réponse. Pour les autres résultats, le comportement actuel écrit le texte fixe `TEST`.
- ModelAndView.java
  - Rôle : objet simple représentant une vue et ses attributs.
  - Le constructeur reçoit l'URL de la vue ; `setAttribute` ajoute/remplace une valeur, `getAttribute` lit une valeur, `getListAttributes` retourne la map complète et `getUrl` retourne l'URL.

Package : com.mnemos.utils
- Utilitaire.java
  - Rôle : coordonne la découverte des contrôleurs, la création/recherche des routes, l'invocation des handlers et plusieurs opérations de conversion et de réponse.
  - `scanPackage` charge les classes trouvées directement dans le package demandé ; `scanControllersInPackage` conserve celles qui portent l'annotation de contrôleur, puis `scanMethod` enregistre les méthodes annotées avec leur instance et leur `UrlMapping`. Une route déjà présente provoque une erreur.
  - `getByUrlMethod` récupère une route ou lève `UrlNotFoundException`. `invoke` injecte dans le contrôleur ses champs de type annoté `@Service` avant l'appel de la méthode.
  - `dynamicCast` prend en charge `int`, `long`, `double`, `boolean`, `String`, `LocalDate`, `LocalDateTime`, `java.sql.Date` et `java.util.Date`. Les autres types non convertibles lèvent une erreur.
  - `toJSON` sérialise avec Jackson ; `sendResponse` écrit la réponse avec un type `application/json` ou `text/plain` déterminé à partir du contenu.
- UrlMethod.java
  - Rôle : clé de la table de routage, composée de l'URL et de la méthode HTTP.
  - `equals` et `hashCode` normalisent la méthode HTTP en majuscules et ignorent la query string dans l'URL.
- RouteMapping.java
  - Rôle : associe une instance de contrôleur à la méthode Java qui traite une route.
- DataBinder.java
  - Rôle : construit le tableau d'arguments d'un handler à partir des paramètres HTTP.
  - Pour un paramètre annoté `@Param`, lit son nom avec `Param.value`, convertit sa valeur et gère les listes de valeurs répétées ainsi que `MapFile`.
  - Pour un paramètre objet sans `@Param`, crée une instance via son constructeur sans argument et renseigne ses champs à partir des paramètres portant les noms des champs.
  - Pour une liste d'objets, regroupe les propriétés reçues avec une notation indexée telle que `objet[0].champ`. La liste doit déclarer un type d'élément concret.
- MapFile.java
  - Rôle : représentation d'un fichier importé contenant son nom, son extension et ses octets.
  - `setFullName` sépare le nom et l'extension ; `setExtension` refuse une extension contenant un point.
  - `getContent` tente un décodage UTF-8 et renvoie une représentation des octets si les données ne sont pas du texte UTF-8 valide. `save(path)` écrit le fichier dans le chemin demandé en créant les répertoires parents manquants.
- RegexParser.java
  - Rôle : extrait des éléments de chaînes correspondant aux formats utilisés pour les paramètres indexés et les noms de fichier.
  - `extractObject` décompose une référence telle que `objet[0].champ` en nom de variable, indice et attribut ; `extractFullParameter` décompose une affectation indexée contenant une valeur ; `extractFileDetail` sépare le nom et l'extension.
  - Les chaînes qui ne correspondent pas aux expressions régulières attendues entraînent une erreur.

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
3. `AppStartListener` construit la table de routage en parcourant le package indiqué par `packageController`, en sélectionnant les classes `@Controller` et en enregistrant leurs méthodes `@UrlMapping`.
4. Le routeur recherche la méthode correspondante :
   - Si aucune méthode n'est trouvée → lève `UrlNotFoundException` (le servlet ne traduit pas actuellement cette exception en code HTTP).
   - Une URL et une méthode HTTP déjà enregistrées provoquent une erreur au scan.
5. Pour la méthode choisie, le résolveur de paramètres instancie les arguments :
   - Pour chaque paramètre `@Param`, le binder lit le nom spécifié et convertit la valeur ; les listes et `MapFile` ont un traitement dédié.
   - Les paramètres objets non annotés sont instanciés puis remplis à partir des paramètres de requête.
6. `Utilitaire.invoke` injecte les champs Spring annotés `@Service`, invoque le handler et récupère son résultat.
7. Le servlet transmet les résultats `ModelAndView` à une vue, sérialise en réponse les méthodes annotées `@WebAPI` et écrit actuellement `TEST` dans les autres cas.
8. `UrlNotFoundException` est levée lorsqu'aucune route n'existe. Le servlet ne définit pas dans son code actuel de traduction générale des exceptions vers des codes HTTP.

## Règles et conventions importantes
- `@Param` nomme une valeur lue dans la requête ; les types convertis par `Utilitaire.dynamicCast` sont ceux décrits plus haut. Les objets complexes sans `@Param` suivent le binding par champs de `DataBinder`.
- Un paramètre `MapFile` doit être annoté avec `@Param` et nécessite une requête multipart ; le servlet est annoté `@MultipartConfig`.
- Seules les classes `@Controller` et les méthodes `@UrlMapping` servent à construire les routes. `@WebAPI` s'applique aux méthodes et sélectionne le traitement JSON de leur résultat.
- `RouteMappingException` et `UrlNotFoundException` existent comme exceptions du framework ; seul `UrlNotFoundException` est actuellement utilisé par le code de routage.
