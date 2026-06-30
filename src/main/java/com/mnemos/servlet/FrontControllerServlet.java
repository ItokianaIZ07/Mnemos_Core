package com.mnemos.servlet;

import com.mnemos.annotation.Controller;
import com.mnemos.annotation.UrlMapping;
import com.mnemos.utils.RouteMapping;
import com.mnemos.utils.UrlMethod;
import com.mnemos.utils.Utilitaire;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class FrontControllerServlet extends HttpServlet {

    private Utilitaire util;
    private List<Class<?>> controllers;
    private Map<UrlMethod, RouteMapping> routes;


    public void init(){
        util = new Utilitaire();
        routes = (Map<UrlMethod, RouteMapping>) getServletContext().getAttribute("routes");
        /* Raha tsy hampiasa listener sinon any ambany */
//        Utilitaire util = new Utilitaire();
//        String packageName = getInitParameter("packageController");
//        try {
//            listController = util.getListController(packageName, Controller.class);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        // D ny init-param anaty web.xml ovaina context-param

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String projectPath = req.getContextPath();
        String URL = req.getRequestURI();
        String method = req.getMethod();
        URL = URL.substring(projectPath.length());
        processRequest(resp, URL, method);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String projectPath = req.getContextPath();
        String URL = req.getRequestURI();
        String method = req.getMethod();
        URL = URL.substring(projectPath.length());
        processRequest(resp, URL, method);
    }

    private void processRequest(HttpServletResponse res, String url, String method) throws IOException {
//        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        UrlMethod um = new UrlMethod(url, method);

        RouteMapping routeMapping = util.getByUrlMethod(um, routes);


        out.println("Controller : " + routeMapping.getController().getName());
        out.println("Nom de la classe : " + routeMapping.getController().getSimpleName());

        out.println("Méthode : " + routeMapping.getMethod().getName());
        out.println("Type de retour methode: "+routeMapping.getMethod().getReturnType().getSimpleName());
        Object invocationResult = util.invoke(routeMapping);
        out.println("Result invocation methode: "+invocationResult);


//        for(Map.Entry<UrlMethod, RouteMapping> entry: routes.entrySet()){
//            UrlMethod urlMethod = entry.getKey();
//            RouteMapping route = entry.getValue();
//
//            out.println("URL: "+urlMethod.getUrl()+" | method: "+urlMethod.getMethod()+" | Controller: "+route.getController().getName()+" | Method: "+route.getMethod().getName());
//
//            if(method.equals(urlMethod.getMethod())){
//                try{
//                    out.println("URL: "+urlMethod.getUrl()+" | method: "+urlMethod.getMethod()+" | Controller: "+route.getController().getName()+" | Method invoqué: "+route.getMethod().getName());
//                    Object returnValue =  util.invoke(route);
//                    out.println("Valeur retournee: " + returnValue);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }


//        for(Map.Entry<String, RouteMapping> entry: methodUrl.entrySet()){
//            String urlAssocie = entry.getKey();
//            RouteMapping route = entry.getValue();
//            out.println("URL: "+url+" | Controller: "+route.getController()+" | Method: "+route.getMethod().getName());
//        }

//        out.println("URL: "+url);

//        for(Map.Entry<String, List<Method>> entry: methodAssocieUrl.entrySet()){
//            String clazz = entry.getKey();
//            List<Method> methods = entry.getValue();
//            out.println(clazz+":");
//            for(Method m: methods){
//                out.println("\t"+m.getName());
//            }
//        }
    }
}
