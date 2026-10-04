package com.mnemos.utils;

import java.util.Objects;

public class UrlMethod {
    private String url;
    private String method;

    public UrlMethod(){}

    public UrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    private String capitalize(String str){
        return str.toUpperCase();
    }

    private String formatUrl(String url){
        int index = url.indexOf('?');
        return (index != -1) ? url.substring(0, index).trim() : url;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        UrlMethod urlMethod = (UrlMethod) object;

        String formatedUrl = formatUrl(url);
        String formatedObjectUrl = formatUrl(urlMethod.url);
        return Objects.equals(formatedUrl, formatedObjectUrl) && Objects.equals(capitalize(method),capitalize(urlMethod.method));
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatUrl(url), capitalize(method));
    }
}
