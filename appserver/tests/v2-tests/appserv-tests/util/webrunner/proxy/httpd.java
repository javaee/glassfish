package com.sun.ejte.ccl.webrunner.proxy;


import java.net.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
* This is the Main class for proxy functionality.http supports GET,POST,HEAD requests.The proxy HTTP server is single
 *threaded,in that each request is handled in turn while all others wait.
* Gets requests from browser and forwards to web server.
* Receives response from web server and forwards to web browser.Writes transaction in a file.
*
* @author       Deepa Singh (deepa.singh@sun.com)
 *Company       Sun Microsystems Inc.
*
*/
public class httpd implements Runnable,LogMessage
{
        private int port;
        private LogMessage log;
        private boolean stopFlag;
        private static String version="1.0";
        private static String CRLF="\r\n";
        private static int buffer_size=8192;
        private String host;
        private String methodSupported;
        private int content_length;

        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param		s	The string to be converted
        * @return		byte[]
        */
        private final byte[] toBytes(String s)
        {
                byte b[]=s.getBytes();
                return b;
        }


        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param		out	The output stream
        * @param		s	The string
        * @return		void
        */
        private void writeString(OutputStream out,String s)
        throws IOException
        {
                out.write(toBytes(s));
        }

        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param		in	The InputStream
        * @param		url	The string URL
        * @param		mh Mimeheader
        * @return		TempStore
        */
        private TempStore loadFile(InputStream in,String url,MimeHeader mh)
        throws IOException
        {
                TempStore temp;
                byte file_buf[]=new byte[buffer_size];
                temp=new TempStore(url,mh);
                int size=0;
                int n;
                while((n=in.read(file_buf))>=0)
                {
                        temp.append(file_buf,n);
                        size+=n;
                }
                in.close();
                return temp;
        }




        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param		cmd	String
        * @param		url	The string URL
        * @param		code integer
        * @param        size Integer
        * @return		void
        */
        private void logEntry(String cmd,String url,int code,int size)
        {

                log.log(host+ "--" + cmd+ "" + url+ "HTTP/1.0 \" " +
                code+ "" +
                size+ "\n");
        }



        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param		in	The InputStream
        * @param		url	The string URL
        * @param		mh Mimeheader
        * @return		TempStore
        */
        private String getRawRequest(InputStream in)
        throws IOException
        {
                byte buf[]=new byte[buffer_size];
                int pos=0;
                int c;
                while((c=in.read()) != -1)
                {
                        switch((char)c)
                        {

                                case '\r':
                                break;

                                case '\n':
                                if(buf[pos-1] == c)
                                {
                                        String temp=new String(buf,0,pos);
                                        //System.out.print("\nString returned is " + temp);
                                        return temp;
                                }

                        default:
                                buf[pos++]=(byte)c;

                        }


                }
                return null;
        }

        /**
         *If browser is set to this proxy server then the requests that will be sent to it will include the complete URL.Complete URL is parsed
         *and remote website name an optional non standard port number is extracted.Then a socket is opened to remote site.A GET or POST request
         * is sent asking for the URL that was sent.Whatever response header is received from the external web server ,it is passed back to the client i.e browser.
         *Transaction is logged in a file if it was success ,socket is closed and method returns.
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param		out	The OutputStream
        * @param		url	The string URL
        * @param		inmh Mimeheader
        * @param		in   The input stream
        * @return		void
        */
        private void handleProxy(OutputStream out,String url,MimeHeader inmh,InputStream in) {
            try {
                int start=url.indexOf("://") +3;
                
                int path=url.indexOf('/',start);
                
                String site=url.substring(start,path).toLowerCase();
                
                String server_url=url.substring(path);
                
                int colon=site.indexOf(':');
                if(colon > 0) {
                    port=Integer.parseInt(site.substring(colon+1));
                    site=site.substring(0,colon);
                }
                
                FileOutputStream fileout=new FileOutputStream("script.txt",true);
                //System.out.print("\n Server at port" + port + "\n");
                Socket server=new Socket(site,port);
                InputStream server_in=server.getInputStream();
                OutputStream server_out=server.getOutputStream();
                inmh.put("User-Agent",inmh.get("User-Agent") + version);
                double first,end;
                if(methodSupported.equalsIgnoreCase("post")) {
                    int i;
                    byte postbuf[]=new byte[content_length];
                    for (i =0; i < content_length; i++)
                        postbuf[i]=(byte)in.read();
                    
                  String postdata=new String(postbuf,0,i);
                  //  System.out.print("\n POST data without mime header is " + postdata + "\n");
                    
                  String post="POST" + " " + server_url+ " " + "HTTP/1.0" + CRLF + inmh
                  + CRLF + postdata+ CRLF+CRLF;
                    
                  System.out.print("\nPOST request sent is " + post);
                  byte filebuf[]=post.getBytes();
                  for( i=0;i<filebuf.length;i++)
                      fileout.write(filebuf[i]);


                    String token="!"+"\n"+"!";
                    byte tokenbytes[]=token.getBytes();
                    for(i=0;i<tokenbytes.length;i++)
                        fileout.write(tokenbytes[i]);

                    fileout.close();

                    first=System.currentTimeMillis();
                    writeString(server_out,post);
                    
                    String raw_request=getRawRequest(server_in);
                    System.out.print("\n Server Response Header is--------------------------\n " + raw_request + "\n");

                    HttpResponse server_response=new HttpResponse(raw_request);
                    writeString(out,server_response.toString());
                    end=System.currentTimeMillis();
                    //System.out.print("\n Server Response Header is--------------------------\n " + server_response.statusCode + "\n");
                    
                    if(server_response.statusCode==200) {
                        
                        TempStore uce=loadFile(server_in,url,server_response.mh);
                        out.write(uce.data,0,uce.length);
                        
                    /*    byte filebuf[]=post.getBytes();
                        for( i=0;i<filebuf.length;i++)
                            fileout.write(filebuf[i]);
                        
                        
                        String token="!"+"\n"+"!";
                        byte tokenbytes[]=token.getBytes();
                        for(i=0;i<tokenbytes.length;i++)
                            fileout.write(tokenbytes[i]);
                        
                        fileout.close();*/
                        
                        logEntry("POST",site + server_url,200,uce.length);
                    }
                    else if(server_response.statusCode==302 || server_response.statusCode==307) {
                        System.out.println("Inside Redirection******************************");
                        String Host=server_response.mh.get("Location");
                        URL newurl=new URL(Host);
                        String actualhost=newurl.getHost();
                        Socket serversocket=new Socket(actualhost,80);
                        start=Host.indexOf("://")+3;
                        path=Host.indexOf('/',start);
                        String redirectURL=Host.substring(path);
                        String redirectreq=new String("GET"+" " +redirectURL+" " + "HTTP/1.0"+CRLF+CRLF);
                        InputStream re_in=serversocket.getInputStream();
                        OutputStream re_out=serversocket.getOutputStream();
                        writeString(re_out,redirectreq);
                        String raw_request1=getRawRequest(re_in);
                        HttpResponse re_response=new HttpResponse(raw_request1);
                        writeString(out,re_response.toString()); //out is proxy's outputstream
                        //now chk the status code and write to proxy's output.Just like above.
                        if(re_response.statusCode==200) {
                    //        System.out.println("Chking redirected request response code"+"*******************");
                            TempStore uce1=loadFile(re_in,Host,re_response.mh);
                            out.write(uce1.data,0,uce1.length);
                            
                            logEntry("GET",Host,200,uce1.length);
                            
                            /*byte filebuf[]=redirectreq.getBytes();
                            for( i=0;i<filebuf.length;i++)
                                fileout.write(filebuf[i]);
                            
                            String token="!"+"\n"+"!";
                            byte tokenbytes[]=token.getBytes();
                            for(i=0;i<tokenbytes.length;i++)
                                fileout.write(tokenbytes[i]);
                            
                            fileout.close();*/
                        }
                    }
                    else if (server_response.statusCode==304) {
                        System.out.println("*************304 status code************************");
                        
                    }
                }
                else {
                    String req=methodSupported + " "  + server_url + " " +"HTTP/1.0" + CRLF + inmh + CRLF + CRLF;
                    
                    System.out.print("\n Request string sent to server\n" + req );
                    byte filebuf2[]=req.getBytes();
                    for(int i=0;i<filebuf2.length;i++)
                        fileout.write(filebuf2[i]);
                    
                    String newtoken="!"+"\n"+"!";
                    byte tokenbytes1[]=newtoken.getBytes();
                    
                    for(int i=0;i<tokenbytes1.length;i++)
                        fileout.write(tokenbytes1[i]);
                    
                    fileout.close();
                    
                    writeString(server_out,req);
                    String raw_request=getRawRequest(server_in);
                    System.out.print("\n Server Response is " + raw_request + "\n");
                    HttpResponse server_response=new HttpResponse(raw_request);
                    writeString(out,server_response.toString());
                    //System.out.print("\n Server Response is " + server_response.statusCode + "\n");
                    
                    if(server_response.statusCode==200) {
                        TempStore uce=loadFile(server_in,url,server_response.mh);
                        out.write(uce.data,0,uce.length);
                    }
                    
                    if(server_response.statusCode==302 || server_response.statusCode==307) {
                        //System.out.println("******Inside Redirection*********");
                        String Host=server_response.mh.get("Location");
                        //System.out.println("New host is"+Host);
                        
                        URL newurl=new URL(Host);
                        String actualhost=newurl.getHost();
                        Socket serversocket=new Socket(actualhost,80);
                        start=Host.indexOf("://")+3;
                        path=Host.indexOf('/',start);
                        String redirectURL=Host.substring(path);
                        //System.out.println("redirected URL is"+redirectURL);
                        String redirectreq="GET" + " " + redirectURL + " "  + "HTTP/1.0"+CRLF+CRLF;
                        //System.out.println("new GET request sent"+redirectreq);
                        InputStream re_in=serversocket.getInputStream();
                        OutputStream re_out=serversocket.getOutputStream();
                        writeString(re_out,redirectreq);
                        String raw_request1=getRawRequest(re_in);
                        HttpResponse re_response=new HttpResponse(raw_request1);
                        writeString(out,re_response.toString()); //out is proxy's outputstream
                        //now chk the status code and write to proxy's output.Just like above.
                        if(re_response.statusCode==200) {
                          //  System.out.println("Chking redirected request response code"+"*******************");
                            TempStore uce1=loadFile(re_in,Host,re_response.mh);
                            out.write(uce1.data,0,uce1.length);
                            //System.out.println("Proxy wrote");
                            //logEntry("GET",Host,200,uce1.length);
                            byte filebuf[]=redirectreq.getBytes();
                            for(int i=0;i<filebuf.length;i++)
                                fileout.write(filebuf[i]);
                            
                            String token="!"+"\n"+"!";
                            byte tokenbytes[]=token.getBytes();
                            for(int i=0;i<tokenbytes.length;i++)
                                fileout.write(tokenbytes[i]);
                            
                            fileout.close();
                        }
                    }
                    if(server_response.statusCode==304) {
                        System.out.println("*******************************304 status code****************************");
                    }
                }
                
                server_out.close();
                
            }
            catch(IOException e) {
                log.log("Exception" + e);
                e.printStackTrace();
            }
            catch(Exception e) {
                log.log("Exception"+e);
                e.printStackTrace();
            }
        }



        /**Called once per connection to the server.It parses the request String and incoming MIME header.
         *Calls handleProxy() if request is of type GET or POST.
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param		s	Socket
        * @return		void
        * @throws 		IOException
        */
    private void doRequest(Socket s) throws IOException
        {
                if(stopFlag==true)
                        return;
                InputStream in=s.getInputStream();
                OutputStream out=s.getOutputStream();
                String request=getRawRequest(in);

                int fsp=request.indexOf(' ');
                int nsp=request.indexOf(' ',fsp+1);
                int eol=request.indexOf('\n');
                String method=request.substring(0,fsp);
                methodSupported=method.toUpperCase();
                String url=request.substring(fsp+1,nsp);
                String raw_mime_header=request.substring(eol+1);

                MimeHeader inmh=new MimeHeader(raw_mime_header);

                String str_length=inmh.get("Content-Length");
                if(str_length!=null)
                {
                      content_length = Integer.parseInt(str_length);
                }
                else
                {
                      content_length = 0;
                 }
                request=request.substring(0,eol);
                if(method.equalsIgnoreCase("get") || method.equalsIgnoreCase("post") || method.equalsIgnoreCase("head"))
                        handleProxy(out,url,inmh,in);
                else
                        writeString(out,"Method Not allowed");
                in.close();
                out.close();
        }

        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @return		void
        *
        */
        public void run()
        {
                try
                {
                ServerSocket acceptSocket;
                 acceptSocket=new ServerSocket(port);
                System.out.print("\nServer Socket created at " + port+ "\n");
                while(true)
                        {
                        Socket s=acceptSocket.accept();
                        doRequest(s);
                        s.close();
                        }
                }
                catch(IOException e)
                {
                log.log("accept loop IOException: " + e + "\n");
                }
                catch(Exception e)
                {
                log.log("Exception :" +e);
                }
        }


        private Thread t=null;


        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @return		void
        *
        */
        public synchronized void start()
        {
                stopFlag=false;
                if(t==null)
                {
                        t=new Thread(this);
                        t.start();
                }
        }


        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @return		void
        *
        */
        public synchronized void stop()
        {
                stopFlag=true;
                log.log("Stopped at" + new Date() + "\n");
        }



        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @return		void
        *
        */
        public httpd(int p, LogMessage lm)
        {
                port =p;
                log=lm;
        }



        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @return		void
        *
        */
        public static void main(String args[])
        {
                System.out.print(" Before Httpd object created");
                httpd h=new httpd(1235,null);
                h.log=h;
                h.start();
                try
        {

                        Thread.currentThread().join();
                }
                catch(InterruptedException e){};
        }



        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @return		void
        *
        */
        public void log(String m)
        {
                System.out.print(m);
        }

}