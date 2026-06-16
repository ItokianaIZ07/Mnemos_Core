package com.mnemos.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Utilitaire {
    private List<Class<?>> listClass;
    public void scanPackage(String packageName) throws IOException, ClassNotFoundException {
        listClass = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/'); // Convertit "com.package" en "com/package"
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        for (File directory : dirs) {
            listClass.addAll(findClasses(directory, packageName));
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

    public List<String> getListController(String packageName, Class<? extends Annotation> annotation) throws InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {
        scanPackage(packageName);
        List<String> controllerList = new ArrayList<>();

        for(Class<?> c: listClass){
            if(c.isAnnotationPresent(annotation)){
                String classFullName = c.getName();
                controllerList.add(classFullName);
            }
        }
        return controllerList;
    }
}
