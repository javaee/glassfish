package org.glassfish.test.jms.injection.ejb;

import java.io.*;

public class GenerateWSConfig {
    public static void main(String[] args) throws Exception {
        String filename = args[0];
        String host = args[1];
        String port = args[2];
        
        StringBuffer config = new StringBuffer();
        config.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\r\n");
        config.append("<configuration xmlns=\"http://java.sun.com/xml/ns/jax-rpc/ri/config\">");
        config.append("\r\n    ");
        config.append("<wsdl location=\"http://").append(host).append(":").append(port);
        config.append("/jms-injection-ws-web/NewWebService?wsdl\" packageName=\"test\"/>");
        config.append("\r\n").append("</configuration>");
    
        File file = new File(filename);
        if (file.exists())
            file.delete();
            
        byte[] output = config.toString().getBytes();
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(output, 0, output.length);
        fos.flush();
        fos.close();
    }
}