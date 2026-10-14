package com.mnemos.reflect;

import com.mnemos.utils.Utilitaire;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class Reflect {

    public static Parameter[] getMethodParameter(Method method){
        Utilitaire.checkCondition(method == null, "Impossible d'appliquer la reflection sur un objet null");
        return method.getParameters();
    }

    public static Field[] getAllObjectFields(Object o){
        Utilitaire.checkCondition(o == null, "Impossible d'appliquer la reflection sur un objet null");
        return o.getClass().getDeclaredFields();
    }

    public static Field[] getPublicObjectFields(Object o){
        Utilitaire.checkCondition(o == null, "Impossible d'appliquer la reflection sur un objet null");
        return o.getClass().getFields();
    }

    public static Method[] getObjectMethods(Object o) {
        Utilitaire.checkCondition(o == null, "Impossible d'appliquer la reflection sur un objet null");

        if (o instanceof Class<?>) {
            return ((Class<?>) o).getDeclaredMethods();
        }

        return o.getClass().getDeclaredMethods();
    }

    public static Object getInstance(Class<?> clazz) {
        try{
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Erreur d'instanciation: "+e);
        }
    }

    public static Method getMethodByName(Object o, String name, String option, Class<?> ...parameterType){
        String methodName = "";
        if(!option.isEmpty()){
            switch (option){
                case "set":
                    methodName = "set"+ Utilitaire.capitalize(name);
                    break;
                case "get":
                    methodName = "get"+Utilitaire.capitalize(name);
                    break;
                default:
                    throw new RuntimeException("Option invalide getMethod");
            }
        }else{
            methodName = name;
        }
        try{
            Method method = o.getClass().getMethod(methodName, parameterType);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Aucune méthode " + methodName + " dans la classe "+o.getClass().getSimpleName(),e);
        }
    }

    public static Object invoke(Method method, Object o, Object... arguments) throws Exception{
        try{
            return method.invoke(o, arguments);
        }catch (Exception e){
            throw new Exception("Une erreur est survenue lors de l'invoquation de la méthode: "+e, e);
        }
    }

    public static Class<?> getListElementType(Parameter p){
        Utilitaire.checkCondition(!List.class.isAssignableFrom(p.getType()), "Le paramètre doit être de type List");

        ParameterizedType parameterizedType = getParameterizedType(p);

        Type[] typeArguments = parameterizedType.getActualTypeArguments(); // ato le type <type 1, type2, ...>

        Utilitaire.checkCondition(typeArguments.length != 1 || !(typeArguments[0] instanceof Class<?>), // hi-verifiena anle hoe ray ihany ny paramètre anle Liste
            "Le type de l'élement de la liste doit être une classe concrète");


        return (Class<?>) typeArguments[0];
    }

    public static Type getListType(Parameter p){
        return p.getParameterizedType(); // List<type quelque chose>
    }

    private static ParameterizedType getParameterizedType(Parameter p) {
        Type parameterType = getListType(p);

        Utilitaire.checkCondition(!(parameterType instanceof ParameterizedType), "Le paramètre doit déclarer un type générique, par exemple List<Integer>");

        ParameterizedType parameterizedType =
                (ParameterizedType) parameterType;
        return parameterizedType; // type quelque chose
    }
}
