package com.mnemos.utils;

import com.mnemos.annotation.Param;
import com.mnemos.reflect.Reflect;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import jdk.jshell.execution.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                value = bindParam(req, p);
            }else{ // raha ohatra hoe objet fa tsy simple param ny anle méthode
//                Utilitaire.checkCondition(List.class.isAssignableFrom(p.getType()),  "Le binding des listes n'est pas encore pris en charge : " + p.getName());

                if(List.class.isAssignableFrom(p.getType())){
                     value = handleListObject(req, p);
                }else{
                    value = Reflect.getInstance(p.getType());
                    bindObject(req, value);
                }
            }
            checkParamValue(value, "Aucune valeur n'est associée au paramètre de type"+p.getType().getSimpleName());

            argsValue.add(value);
        }
        return !argsValue.isEmpty() ? argsValue.toArray() : null;
    }

    private Object bindParam(HttpServletRequest req, Parameter p){
        if(List.class.isAssignableFrom(p.getType())){
            return handleListArgs(req, p);
        }
        if(p.getType().equals(MapFile.class)){
            return handleFile(req, p);
        }
        String parameterName = this.util.getParameterName(p);
        String paramValue = req.getParameter(parameterName);
        checkParamValue(paramValue, "Le paramètre "+parameterName+" est obligatoire");

        return this.util.dynamicCast(paramValue, p.getType());
    }

    private Object handleFile(HttpServletRequest req, Parameter p){
        MapFile mapFile = new MapFile();
        try {
            Part filePart = req.getPart(util.getParameterName(p));
            mapFile.setFullName(filePart.getSubmittedFileName());
            mapFile.setData(util.readInputStream(filePart.getInputStream()));

            return mapFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object handleListArgs(HttpServletRequest req, Parameter p){
        Class<?> listParameterType = Reflect.getListElementType(p);
        List<Object> list = new ArrayList<>();

//            Method method = Reflect.getMethodByName(list, "add", "", listParameterType);
        String paramName = util.getParameterName(p);
        String[] params = req.getParameterValues(paramName);
        for(String param: params){
            list.add(util.dynamicCast(param, listParameterType));
        }

        return list;
    }

    private void bindObject(HttpServletRequest req, Object o){

        Field[] fields = Reflect.getAllObjectFields(o);
        for(Field f: fields){
            Method method = Reflect.getMethodByName(o, f.getName(), "set", f.getType());
            try{
                String paramValue = req.getParameter(f.getName());
//               checkParamValue(paramValue, "Le paramètre "+f.getName()+" est obligatoire");

                Reflect.invoke(method, o, this.util.dynamicCast(paramValue, f.getType()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    private Object handleListObject(HttpServletRequest req, Parameter p) {
        Map<String, String[]> parameters = req.getParameterMap();
        Class<?> paramType = Reflect.getListElementType(p);

        // Associer chaque index à un seul objet métier
        Map<Integer, Object> objects = new TreeMap<>();


        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String param = entry.getKey();

            Map<String, String> objectParam =
                    RegexParser.extractObject(param);

            // Ignorer les paramètres qui ne correspondent pas au format
            if (objectParam == null) {
                continue;
            }

            String attribut = objectParam.get("attribut");
            int indice;

            try {
                indice = Integer.parseInt(objectParam.get("indice"));
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        "Erreur de conversion de l'indice : " + param, e
                );
            }

            String[] values = entry.getValue();

            Utilitaire.checkCondition(
                    values.length != 1,
                    "Le paramètre " + param + " doit prendre une seule valeur"
            );

            String valeur = values[0];

            // Ref tsy mi-existe le objet eo @le indice de executena le fonction sinon retourne directement le objet
            Object paramObject = objects.computeIfAbsent(
                    indice,
                    i -> Reflect.getInstance(paramType)
            );

            Class<?> attributType = Reflect.getDeclaredField(Reflect.getInstance(paramType), attribut).getType();

            Method method = Reflect.getMethodByName(paramObject, attribut, "set", attributType);

            try {
                Reflect.invoke(method, paramObject, util.dynamicCast(valeur, attributType));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return new ArrayList<>(objects.values());
    }
}
