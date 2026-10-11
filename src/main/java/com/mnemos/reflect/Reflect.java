package com.mnemos.reflect;

import com.mnemos.utils.Utilitaire;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Reflect {
    private static void checkValue(Object o){
        if(o == null){
            throw new RuntimeException("Impossible d'appliquer la reflection sur un objet null");
        }
    }

    public static Parameter[] getMethodParameter(Method method){
        checkValue(method);
        return method.getParameters();
    }

    public static Field[] getAllObjectFields(Object o){
        checkValue(o);
        return o.getClass().getDeclaredFields();
    }

    public static Field[] getPublicObjectFields(Object o){
        checkValue(o);
        return o.getClass().getFields();
    }

    public static Method[] getObjectMethods(Object o) {
        checkValue(o);

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

    public static Method getMethodByName(Object o, Field field, String option){
        String methodName = "";
        try{
            switch (option){
                case "set":
                    methodName = "set"+ Utilitaire.capitalize(field.getName());
                    break;
                case "get":
                    methodName = "get"+Utilitaire.capitalize(field.getName());
                    break;
                default:
                    throw new RuntimeException("Option invalide getMethod");
            }
            Method method = o.getClass().getDeclaredMethod(methodName, field.getType());
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
}
