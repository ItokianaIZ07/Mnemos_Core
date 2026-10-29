package com.mnemos.servlet;

import com.mnemos.annotation.Auth;
import com.mnemos.annotation.PermitRole;
import com.mnemos.exception.NoUserConnected;
import com.mnemos.exception.RoleNotAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import javax.management.relation.RoleNotFoundException;
import java.lang.reflect.Method;
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

    public static void checkSession(HttpServletRequest req, Map<String, Session> sessions, String authKey){
        HttpSession session = req.getSession(true);
        Session userSession = sessions.get(session.getId());
        if(userSession == null){
            throw new RuntimeException("Aucune session a vérifié pour l'authetification");
        }

        if(!userSession.getData().containsKey(authKey) || userSession.getData(authKey) == null){
            throw new NoUserConnected("Veuillez vous connecté pour effectuer cette action");
        }
    }

    public static void checkSession(HttpServletRequest req, Map<String, Session> sessions, String authKey, String roleKey, Method method) {
        HttpSession httpSession = req.getSession(true);
        Session session = sessions.get(httpSession.getId());

        if(!method.isAnnotationPresent(Auth.class) && method.isAnnotationPresent(PermitRole.class)){
            throw new RuntimeException("L'annotation PermitRole doit être utilisé avec l'annotation Auth");
        }
        if(!session.getData().containsKey(authKey) || session.getData(authKey) == null){
            throw new NoUserConnected("Veuillez vous connecter pour effectuer cette action");
        }
        if(!session.getData().containsKey(roleKey) || session.getData(roleKey) == null){
            throw new RuntimeException("Veuillez vous connecter avec le rôle nécessaire pour effectuer cette action");
        }
        String sessionRole = session.getData(roleKey).toString();
        String[] allowedRole = method.getAnnotation(PermitRole.class).value();
        for(String role: allowedRole){
            if(sessionRole.equalsIgnoreCase(role)){
                return;
            }
        }
        throw new RoleNotAllowed("Veuillez vous authentifié avec le rôle adapté");
    }
}
