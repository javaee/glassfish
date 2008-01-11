package com.sun.enterprise.admin.cli.deployment;

import java.net.*;
import java.io.*;
import java.util.*;

public class FileUploadUtil {

        /**
         * upload file content to server using http post
         * @param httpConnection - http url
         * @param fileName - file name to upload to server
         * @returns HttpURLConnection - allows the caller of this api to
         *          capture server response and return code
         * @exception Exception - exception from uploading file
         **/
    public static HttpURLConnection upload(final String httpConnection, final File fileName) throws Exception {
        HttpURLConnection urlConnection = null;
        
        try {
            URL url = new URL(httpConnection);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("User-Agent", "hk2-cli");
            urlConnection.connect();

            OutputStream out = urlConnection.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));

            // write upload file data
            byte[] buffer = new byte[1024*64];
            for (int i = bis.read(buffer); i > 0; i = bis.read(buffer)) {
                out.write(buffer, 0, i);
            }
            out.flush();
            if (bis != null) {
                bis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlConnection;  //return null for now
    }

        //main clase used for testing
    public static void main (String [] args) {
        try {
            if (args.length>2) {
                final File fileName = new File(args[2]);
                final String httpConnection = "http://"+args[0]+":"+args[1]+"/__asadmin/deploy?path="+
                                               URLEncoder.encode(fileName.getName(), "UTF-8")+
                                              "?upload="+URLEncoder.encode("true", "UTF-8");
                System.out.println("httpConnection = " + httpConnection);
                FileUploadUtil.upload(httpConnection, fileName);
            } else {
                System.out.println("usage: FileUploadUtil <host> <port> <filename>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
