package com.mnemos.servlet;

import com.mnemos.exception.NoUserConnected;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {
    private Map<String, Object> data;

    public Session() {
        data = new HashMap<>();
    }

    public void setData(Map<String, Object> data){
        this.data = data;
    }

    public void setData(String key, Object value){
        data.put(key, value);
    }

    public Object getData(String key){
        return this.data.get(key);
    }

    public Map<String, Object> getData(){
        return this.data;
    }

    public Object removeData(String key){
        return this.data.remove(key);
    }

    public void clear(){
        this.data.clear();
    }

    @Override
    public String toString() {
        return "Session{" +
                "data=" + data +
                '}';
    }

    public static void checkSession(HttpServletRequest req, Map<String, Session> sessions, String id){
        HttpSession session = req.getSession(true);
        Session userSession = sessions.get(session.getId());
        if(!userSession.data.containsKey(id) || userSession.data.get(id) == null){
            throw new NoUserConnected("Veuillez vous connecté pour effectuer cette action");
        }
    }
}
