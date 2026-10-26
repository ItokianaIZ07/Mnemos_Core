package com.mnemos.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

public class MapFile {
    private String fileName;
    private String extension;
    private byte[] data;
    private String path;

    public MapFile(){}

    public MapFile(String fileName, String extension, byte[] data) {
        this.fileName = fileName;
        this.setExtension(extension);
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        if(extension.contains(".")){
            throw new RuntimeException("L'extension ne dois pas contenir de '.'");
        }
        this.extension = extension;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void save(String path){

        Path savePath = Paths.get(path, this.fullName());

        try {
            if (savePath.getParent() != null) {
                Files.createDirectories(savePath.getParent());
            }

            Files.write(savePath, this.data);
            this.path = path;
            System.out.println("Fichier enregistré avec succès !");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier "+this.fullName()+" :"+ e.getMessage());
        }
    }

    public void setFullName(String name){
        Map<String, String> fileDetails = RegexParser.extractFileDetail(name);
        setFileName(fileDetails.get("fileName"));
        setExtension(fileDetails.get("extension"));
    }

    public String fullName(){
        return fileName+"."+extension;
    }

    public String getContent() {
        CharsetDecoder decoder = StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try{
            return decoder.decode(ByteBuffer.wrap(this.data)).toString();
        } catch (CharacterCodingException e) {
            return Arrays.toString(this.data);
        }
    }

    public String getPath(){
        if(this.path == null){
            throw new RuntimeException("Ce fichier n'as pas encore été sauvegargé");
        }
        return this.path;
    }
}
