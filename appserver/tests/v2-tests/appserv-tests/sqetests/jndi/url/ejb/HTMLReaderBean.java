/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1peqe.jndi.url.ejb;

import javax.naming.*;
import javax.ejb.*;
import java.net.*;
import java.io.*;

public class HTMLReaderBean implements SessionBean {
 
  public StringBuffer getContents() throws HTTPResponseException {

     Context context;
     URL url;
     StringBuffer buffer;
     String line;
     int responseCode;
     HttpURLConnection connection;
     InputStream input;
     BufferedReader dataInput;
 
     try {
        context = new InitialContext();
        url = (URL)context.lookup("java:comp/env/url/MyURL");  
        connection = (HttpURLConnection)url.openConnection();
        responseCode = connection.getResponseCode();
     } catch (Exception ex) {
         throw new EJBException(ex.getMessage());
     }

     if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new HTTPResponseException("HTTP response code: " + 
           String.valueOf(responseCode));
     }

     try {
        buffer = new StringBuffer();
        input = connection.getInputStream();
        dataInput = new BufferedReader(new InputStreamReader(input));
        while ((line = dataInput.readLine()) != null) {
           buffer.append(line);
           buffer.append('\n');
        }  
     } catch (Exception ex) {
         throw new EJBException(ex.getMessage());
     }

     return buffer;

  } // getContents()

   public HTMLReaderBean() {}
   public void ejbCreate() {}
   public void ejbRemove() {}
   public void ejbActivate() {}
   public void ejbPassivate() {}
   public void setSessionContext(SessionContext sc) {}

} // HTMLReaderBean
