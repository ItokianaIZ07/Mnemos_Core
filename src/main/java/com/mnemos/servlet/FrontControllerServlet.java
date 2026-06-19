package com.mnemos.servlet;

import com.mnemos.annotation.Controller;
import com.mnemos.utils.Utilitaire;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FrontControllerServlet extends HttpServlet {

    List<String> listController;

    public void init(){
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
        String URL = req.getRequestURI();
        processRequest(resp, URL);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getContextPath();
        processRequest(resp, URL);
    }

    private void processRequest(HttpServletResponse res, String url) throws IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        List<String> controllera = (List<String>) getServletContext().getAttribute("controllers");
        for(String clazz: controllera){
            out.println(clazz+"\n");
        }
    }
}
