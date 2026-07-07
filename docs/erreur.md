# 📘 Problème rencontré dans le FrontControllerServlet (Framework MVC maison)

## 1. Contexte

Un `FrontControllerServlet` a été développé pour gérer les routes de l’application via un système personnalisé :

* `@Controller`
* `@UrlMapping`
* `ModelAndView`
* `RouteMapping`

Le servlet est responsable de :

* recevoir toutes les requêtes HTTP
* trouver la méthode associée à l’URL
* exécuter le contrôleur
* rediriger vers une vue JSP via `RequestDispatcher.forward()`

---

# ❌ 2. Erreur rencontrée

Lors de l’utilisation du mapping suivant dans `web.xml` :

```xml
<url-pattern>/*</url-pattern>
```

une erreur critique est apparue :

```
java.lang.StackOverflowError
```

---

# 🔍 3. Cause du problème

## 3.1 Compréhension du mapping `/*`

Le pattern :

```xml
/*
```

signifie :

> Toutes les requêtes HTTP sont interceptées, y compris les ressources internes.

Cela inclut :

* `/home`
* `/login`
* `/WEB-INF/views/index.jsp`
* même les requêtes internes générées par `forward()`

---

## 3.2 Boucle infinie du Forward

Le problème principal est le suivant :

1. L’utilisateur appelle :

```
/home
```

2. Le FrontController traite la requête et exécute :

```java
forward("/WEB-INF/views/index.jsp")
```

3. Tomcat reçoit la requête JSP

4. MAIS comme `/*` intercepte tout, **la requête repasse dans le FrontController**

5. Le FrontController relance encore un `forward()`

6. Le cycle se répète indéfiniment

---

## 🔁 3.3 Résultat

Cette boucle infinie provoque :

```text
FrontController → forward → FrontController → forward → FrontController → ...
```

➡️ La pile d’appels Java se remplit jusqu’à saturation

---

# 💥 4. Signification du StackOverflowError

## 4.1 Définition

`StackOverflowError` signifie :

> La pile d’appels des méthodes est saturée à cause d’appels récursifs infinis.

---

## 4.2 Pourquoi cela arrive ici ?

Dans ce cas précis :

* chaque `forward()` réactive le FrontController
* aucune condition d’arrêt n’empêche la récursion
* les appels s’empilent sans fin

---

## 4.3 Illustration simple

```text
FrontController.process()
    ↓
forward()
    ↓
FrontController.process()
    ↓
forward()
    ↓
FrontController.process()
    ↓
...
```

➡️ La pile mémoire explose

---

# ✅ 5. Solution appliquée

Le mapping a été corrigé :

```xml
<url-pattern>/</url-pattern>
```

---

## 5.1 Pourquoi cela corrige le problème ?

Avec `/` :

* le FrontController reçoit uniquement les requêtes utilisateur
* les `forward()` vers `/WEB-INF/...` ne réactivent pas le servlet

---

## 5.2 Résultat

* Plus de boucle infinie
* Les JSP sont traitées correctement par Tomcat
* Le framework fonctionne comme un DispatcherServlet simplifié

---

# 📌 6. Conclusion

L’erreur venait d’une mauvaise configuration du mapping servlet :

| Mauvaise configuration | Problème                                                |
| ---------------------- | ------------------------------------------------------- |
| `/*`                   | Intercepte aussi les forwards internes → boucle infinie |

| Bonne configuration | Résultat                                                             |
| ------------------- | -------------------------------------------------------------------- |
| `/`                 | Intercepte uniquement les requêtes externes → fonctionnement correct |

---

# 🚀 7. Leçon importante

Dans un framework MVC :

* `/*` est dangereux car il intercepte tout
* `/` est le mapping standard des FrontControllers (comme Spring MVC)
* `forward()` doit sortir du cycle du contrôleur, pas le réactiver
