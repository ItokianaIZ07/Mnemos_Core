package com.mnemos.utils;

import com.mnemos.annotation.Param;
import com.mnemos.reflect.Reflect;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class DataBinder {
    private Utilitaire util;

    public DataBinder(Utilitaire util){
        this.util = util;
    }

    public void checkParamValue(Object value, String errorMessage){
        if(value == null || value.toString().isEmpty()){
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public Object[] getRequestArguments(HttpServletRequest req, RouteMapping routeMapping){
        /*
         * Donnée attendu param=value&param=value(application/x-www-form-urlencoded) et non JSON
         * */

        Parameter[] parameters = Reflect.getMethodParameter(routeMapping.getMethod());

        List<Object> argsValue = new ArrayList<>();
        for(Parameter p: parameters){
            Object value = null;
            if(p.isAnnotationPresent(Param.class)){
                String parameterName = this.util.getParameterName(p);
                String paramValue = req.getParameter(parameterName);
                checkParamValue(paramValue, "Le paramètre "+parameterName+" est obligatoire");

                value = this.util.dynamicCast(paramValue, p.getType());
            }else{ // raha ohatra hoe objet fa tsy simple param ny anle méthode
                value = Reflect.getInstance(p.getType());
                Field[] fields = Reflect.getAllObjectFields(value);
                for(Field f: fields){
                    Method method = Reflect.getMethodByName(value, f, "set");
                    try{
                        String paramValue = req.getParameter(f.getName());
//                        checkParamValue(paramValue, "Le paramètre "+f.getName()+" est obligatoire");

                        Reflect.invoke(method, value, this.util.dynamicCast(paramValue, f.getType()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            checkParamValue(value, "Aucune valeur n'est associée au paramètre "+p.getName());

            argsValue.add(value);
        }
        return !argsValue.isEmpty() ? argsValue.toArray() : null;
    }
}
