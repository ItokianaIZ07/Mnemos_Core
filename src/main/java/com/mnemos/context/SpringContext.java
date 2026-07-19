package com.mnemos.context;

import org.springframework.context.ApplicationContext;

public class SpringContext {
    private ApplicationContext context;

    public SpringContext(ApplicationContext context){
        this.context = context;
    }

    public void setContext(ApplicationContext context){
        this.context = context;
    }

    public ApplicationContext getContext(){
        return this.context;
    }

    public <T> T getBean(Class<T> name){
        if(context == null){
            throw new RuntimeException("SpringContext n'est pas encore initialisé");
        }
        return this.context.getBean(name);
    }
}
