package com.mnemos.listener;

import com.mnemos.annotation.Controller;
import com.mnemos.annotation.UrlMapping;
import com.mnemos.utils.RouteMapping;
import com.mnemos.utils.UrlMethod;
import com.mnemos.utils.Utilitaire;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebListener
public class AppStartListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent contextEvent) {
        ServletContext context = contextEvent.getServletContext();
        Utilitaire util = new Utilitaire();
        String packageName = context.getInitParameter("packageController");
        String prefix = context.getInitParameter("prefix");
        String suffix = context.getInitParameter("suffix");
        try {
//            List<String> controllers = util.getListController(packageName, Controller.class);
            Map<UrlMethod, RouteMapping> routes = new HashMap<>();

            util.scanControllersInPackage(packageName, routes, Controller.class, UrlMapping.class);

            context.setAttribute("routes", routes);
            context.setAttribute("prefix", prefix);
            context.setAttribute("suffix", suffix);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {
    }
}
