package com.mnemos.servlet;

import com.mnemos.annotation.Controller;
import com.mnemos.annotation.UrlMapping;
import com.mnemos.context.SpringContext;
import com.mnemos.utils.RouteMapping;
import com.mnemos.utils.UrlMethod;
import com.mnemos.utils.Utilitaire;
import jakarta.servlet.RequestDispatcher;
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
    private String suffix;
    private String prefix;
    private SpringContext context;


    public void init(){
        util = new Utilitaire();
        routes = (Map<UrlMethod, RouteMapping>) getServletContext().getAttribute("routes");
        prefix = getServletContext().getAttribute("prefix").toString();
        suffix = getServletContext().getAttribute("suffix").toString();
        context = (SpringContext) getServletContext().getAttribute("springContext");
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
        String URI = req.getRequestURI();
        String method = req.getMethod();
        String URL = URI.substring(projectPath.length());
        processRequest(req, resp, URL, method);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String projectPath = req.getContextPath();
        String URL = req.getRequestURI();
        String method = req.getMethod();
        URL = URL.substring(projectPath.length());
        processRequest(req, resp, URL, method);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res, String url, String method) throws IOException, ServletException {
        UrlMethod um = new UrlMethod(url, method);
        RouteMapping routeMapping = util.getByUrlMethod(um, routes);
        if(req.getMethod().equals("GET")){
            Object objectView;
            if((objectView = util.invoke(routeMapping, context)) instanceof ModelAndView){
                ModelAndView modelAndView = (ModelAndView) objectView;
                Map<String, Object> attributes = modelAndView.getListAttributes();
                String view = prefix+modelAndView.getUrl()+suffix;  
                util.setRequestAttributes(req, attributes);
                RequestDispatcher dispatcher = req.getRequestDispatcher(view);
                dispatcher.forward(req, res);
            }
        }
    }


//        out.println("Controller : " + routeMapping.getController().getName());
//        out.println("Nom de la classe : " + routeMapping.getController().getSimpleName());
//
//        out.println("Méthode : " + routeMapping.getMethod().getName());
//        out.println("Type de retour methode: "+routeMapping.getMethod().getReturnType().getSimpleName());
//        Object invocationResult = util.invoke(routeMapping);
//        out.println("Result invocation methode: "+invocationResult);


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