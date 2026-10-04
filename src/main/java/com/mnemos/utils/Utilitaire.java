package com.mnemos.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnemos.annotation.Param;
import com.mnemos.annotation.WebAPI;
import com.mnemos.annotation.UrlMapping;
import com.mnemos.context.SpringContext;
import com.mnemos.exception.UrlNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

public class Utilitaire {
    private List<Class<?>> listController;

    public void scanPackage(String packageName) throws IOException, ClassNotFoundException {
        listController = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/'); // Convertit "com.package" en "com/package"
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        for (File directory : dirs) {
            listController.addAll(findClasses(directory, packageName));
        }
    }

    private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    classes.add(Class.forName(className));
                }
            }
        }
        return classes;
    }

    public void scanControllersInPackage(String packageName, Map<UrlMethod, RouteMapping> routes, Class<? extends Annotation> annotationController, Class<? extends Annotation> annotationMethod) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        scanPackage(packageName);

        for (Class<?> c : listController) {
            if (c.isAnnotationPresent(annotationController)) {
                scanMethod(routes, c, annotationMethod);
            }
        }
    }

    public void scanMethod(Map<UrlMethod, RouteMapping> routes, Class<?> controller, Class<? extends Annotation> annotation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!annotation.isAssignableFrom(UrlMapping.class)) {
            throw new RuntimeException("Invalid annotation type");
        }
        Method[] methods = controller.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotation)) {
                UrlMapping urlMapping = (UrlMapping) method.getAnnotation(annotation);
                String link = urlMapping.url();
                String requestMethod = urlMapping.method();
                UrlMethod um = new UrlMethod(link, requestMethod);
                RouteMapping route = new RouteMapping(controller.getConstructor().newInstance(), method);

                if (routes.containsKey(um)) {
                    throw new RuntimeException("L'url " + link + " est déjà utiliser dans " + routes.get(um).getMethod().getName());
                }
                routes.put(um, route);
            }
        }
    }

    public Object invoke(RouteMapping routeMapping, SpringContext context, Object... args) {
        try {
            Object controller = routeMapping.getController();
            Method method = routeMapping.getMethod();
            setFieldsValue(controller, context);

            return method.invoke(controller, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFieldsValue(Object controller, SpringContext context) throws IllegalAccessException {
        for (Field field : controller.getClass().getDeclaredFields()) {
            Class<?> type = field.getType();
            if (type.isAnnotationPresent(Service.class)) {
                Object service = context.getBean(type);
                field.setAccessible(true);
                field.set(controller, service);
            }
        }
    }

    public RouteMapping getByUrlMethod(UrlMethod urlMethod, Map<UrlMethod, RouteMapping> routes) {
        RouteMapping route = routes.get(urlMethod);
        StringBuilder message = new StringBuilder("Aucune methode associer a l'url: " + urlMethod.getUrl() + "\n");
        for (Map.Entry<UrlMethod, RouteMapping> entry : routes.entrySet()) {

            String url = entry.getKey().getUrl();
            String httpMethod = entry.getKey().getMethod();
            String methodName = entry.getValue().getMethod().getName();
            String controllerName = entry.getValue().getController().getClass().getSimpleName();

            message.append("URL: ")
                    .append(url)
                    .append("\tMethode: ")
                    .append(methodName)
                    .append("\tMethode HTTP: ")
                    .append(httpMethod)
                    .append("\tClasse: ")
                    .append(controllerName)
                    .append("\n");
        }
        if (route == null) {
            throw new UrlNotFoundException(message.toString());
        }
        return route;
    }

    public void setRequestAttributes(HttpServletRequest req, Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public boolean isMethodReturnJSON(Method method) {
        return method.isAnnotationPresent(WebAPI.class);
    }

    public String toJSON(Object o) {
        if(o instanceof String){
            return o.toString();
        }

        ObjectMapper mapper = new ObjectMapper();

        String json = "";

        try{
            json = mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(o);
        }catch (JsonProcessingException e){
            throw new RuntimeException("Une erreur est survenue lors de la formatage en json: "+e);
        }

//        Field[] fields = o.getClass().getDeclaredFields();
//        System.out.println(fields.length);
//        String json = "";
//        StringJoiner sj = new StringJoiner(",");
//
//        for (Field f : fields) {
//            System.out.println(f.getName());
//            try{
//                f.setAccessible(true);
//                Object value = f.get(o);
//                boolean isNumber = checkValue(value);
//                json += f.getName() + ":"+value;
//                sj.add("\""+f.getName() + "\""+":" + (isNumber ? value : "\""+value+"\""));
//            }catch (Exception e){
//                throw new RuntimeException("nisy erreur b: \n"+e);
//            }
//        }
//        if(sj.length() != 0){
//            json = "{"+ sj +"}";
//        }

        return json;
    }

    public Object[] getRequestArguments(HttpServletRequest req, RouteMapping routeMapping){
        /*
        * Donnée attendu param=value&param=value(application/x-www-form-urlencoded) et non JSON
        * */

//        Enumeration<String> parameterNames = req.getParameterNames();
        Parameter[] parameters = getMethodParameter(routeMapping.getMethod());

        List<Object> argsValue = new ArrayList<>();
        for(Parameter p: parameters){
            String parameterName = getParameterName(p);
            String paramValue = req.getParameter(parameterName);
            if(paramValue == null){
                throw new IllegalArgumentException("Le paramètre "+parameterName+" est obligatoire");
            }
            Object value = dynamicCast(paramValue, p.getType());
            argsValue.add(value);
        }
//        tsy nampiasaina satria lasa tsy mifanaraka ny ordre anle paramètre
//        while(parameterNames.hasMoreElements()){
//            String paramName = parameterNames.nextElement();
//            String paramValue = req.getParameter(paramName);
//            for(Parameter p: parameters){
//                String parameter = getParameterName(p);
//                if(parameter.equals(paramName)){
//                    Object value = dynamicCast(paramValue, p.getType());
//                    argsValue.add(value);
//                }
//            }
//        }
        return !argsValue.isEmpty() ? argsValue.toArray() : null;
    }

    public String getParameterName(Parameter parameter){
        if(parameter.isAnnotationPresent(Param.class)){
            return parameter.getAnnotation(Param.class).value();
        }

        throw new RuntimeException("Les paramètres des méthodes du controller doivent être annoté avec @Param");
    }

    public Parameter[] getMethodParameter(Method method){
        return method.getParameters();
    }

    public Object dynamicCast(String value, Class<?> targetType) {

        if (value == null) {
            if (targetType == int.class) return 0;
            if (targetType == boolean.class) return false;
            if (targetType == double.class) return 0.0;
            if (targetType == long.class) return 0L;

            return null;
        }

        value = value.trim();

        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }

        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }

        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if (targetType == String.class) {
            return value;
        }

        if (targetType == java.time.LocalDate.class) {
            return java.time.LocalDate.parse(value);
        }

        if (targetType == java.time.LocalDateTime.class) {
            return java.time.LocalDateTime.parse(value);
        }

        if (targetType == java.sql.Date.class) {
            return java.sql.Date.valueOf(
                    java.time.LocalDate.parse(value)
            );
        }

        if (targetType == java.util.Date.class) {
            return java.sql.Timestamp.valueOf(
                    java.time.LocalDateTime.parse(value)
            );
        }

        try {
            return targetType.cast(value);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Impossible de convertir '" + value +
                            "' vers " + targetType.getName()
            );
        }
    }

    private boolean checkValue(Object value){
        if(value instanceof Number){
            return true;
        }
        return false;
    }

}
