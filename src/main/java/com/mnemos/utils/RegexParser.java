package com.mnemos.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser {
    public static Map<String, String> extractFullParameter(String chaine) {

        String regex = "\\[(\\d+)\\]\\.([^=]+)=(.*)"; // ref de type var[indice].attribut=valeur

        // Compilation du pattern
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(chaine);

        if (matcher.find()) {
            Map<String , String> requestContent = new HashMap<>();
            String indice = matcher.group(1);
            String attribut = matcher.group(2);
            String valeur = matcher.group(3);

            requestContent.put("indice", indice);
            requestContent.put("attribut", attribut);
            requestContent.put("valeur", valeur);

            return requestContent;
        }

        return new HashMap<>();
    }

    public static Map<String, String> extractObject(String chaine){
        String regex = "^([a-zA-Z_$][a-zA-Z0-9_$]*)\\[(\\d+)\\]\\.([a-zA-Z_$][a-zA-Z0-9_$]*)$"; // ref de type var[indice].attribut

        // Compilation du pattern
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(chaine);

        if (matcher.find()) {
            Map<String , String> requestContent = new HashMap<>();
            String variable = matcher.group(1);
            String indice = matcher.group(2);
            String attribut = matcher.group(3);

            requestContent.put("variable", variable);
            requestContent.put("indice", indice);
            requestContent.put("attribut", attribut);

            return requestContent;
        }

        return null;
    }
}
