package com.mnemos.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser {
    public static Map<String, String> extract(String chaine) {

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
}
