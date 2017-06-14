package com.sun.enterprise.config;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import java.io.FileReader;
import java.io.File;

public class MyHandler extends DefaultHandler
{
    public InputSource resolveEntity (String publicId, String systemId) {
        try {
            return new InputSource(new FileReader (new File("domain.dtd")));
        }
        catch (Exception e){
            return null;
        }
            
    }
}
