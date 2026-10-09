package com.mnemos.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class DataBinder {
    private Utilitaire util;

    public DataBinder(Utilitaire util){
        this.util = util;
    }

    public Object[] getRequestArguments(HttpServletRequest req, RouteMapping routeMapping){
        /*
         * Donnée attendu param=value&param=value(application/x-www-form-urlencoded) et non JSON
         * */

        Parameter[] parameters = this.util.getMethodParameter(routeMapping.getMethod());

        List<Object> argsValue = new ArrayList<>();
        for(Parameter p: parameters){
            String parameterName = this.util.getParameterName(p);
            String paramValue = req.getParameter(parameterName);
            if(paramValue == null){
                throw new IllegalArgumentException("Le paramètre "+parameterName+" est obligatoire");
            }
            Object value = this.util.dynamicCast(paramValue, p.getType());
            argsValue.add(value);
        }
        return !argsValue.isEmpty() ? argsValue.toArray() : null;
    }
}
