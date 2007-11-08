/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.grizzly.async.javamail;

import com.sun.enterprise.web.connector.grizzly.AsyncExecutor;
import com.sun.enterprise.web.connector.grizzly.AsyncFilter;
import com.sun.enterprise.web.connector.grizzly.AsyncHandler;
import com.sun.enterprise.web.connector.grizzly.AsyncTask;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.naming.InitialContext;

import com.sun.enterprise.deployment.MailConfiguration;
import com.sun.mail.pop3.POP3SSLStore;
import javax.mail.URLName;
import org.apache.coyote.Request;

/**
 * This <code>AsyncFilter</code> connect to an email account and look for 
 * new messages. If there is no new message, the connection is keep-alived
 * until a new message arrives. This is just an example on how asynchronous
 * request processing works in Grizzly.
 *
 * @author Jeanfrancois Arcand
 */
public class JavaMailAsyncFilter implements AsyncFilter {
    
    /**
     * Collection used to store <code>JavaMailAsyncHandler</code> registration.
     */
    private static ConcurrentHashMap<String,JavaMailAsyncFilterHandler> handlers 
       = new ConcurrentHashMap<String,JavaMailAsyncFilterHandler>();    
    
    
    /**
     * Cache instance of <code>MailFetcher</code>
     */
    private static ConcurrentLinkedQueue<MailFetcher> mailFetcherCache =
            new ConcurrentLinkedQueue<MailFetcher>();
            
    
    /**
     * Scheduler used to wait between execution of 
     * <code>JavaMailAsyncHandler</code>
     */
    private final static ScheduledThreadPoolExecutor scheduler 
            = new ScheduledThreadPoolExecutor(1);
    
    
    /**
     * The current JavaMal Session.
     */
    private Session mailSession;
    
    
    /**
     * Properties used to store JavaMal configuration.
     */
    private Properties props;

    
    // ---------------------------------------------------------------------//
    
    
    /**
     * <code>AsyncFilter</code> which implement a JavaMail client. 
     */
    public JavaMailAsyncFilter() {
        try {
            InitialContext ic = new InitialContext();
            String snName = "mail/MailSession";
            MailConfiguration mailConfig = 
                     (MailConfiguration)ic.lookup(snName); 
            
            props = mailConfig.getMailProperties();      
            props.setProperty("mail.pop3.ssl", "true");
            String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
            props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
            props.setProperty("mail.pop3.socketFactory.fallback", "false");
 
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Register a <code>JavaMailAsyncFilterHandler</code>
     */ 
    public static void register(String contextPath,
                                JavaMailAsyncFilterHandler handler){        
        handlers.put(contextPath,handler);
    }
       
    
    /**
     * Execute the the filter by looking at the request context path. If
     * the context path is a registered <code>JavaMailAsyncFilterHandler</code>,
     * the request will be executed asynchronously. If not, the request will
     * be executed synchronously.
     *
     **/
    public boolean doFilter(AsyncExecutor asyncExecutor) {
        AsyncTask asyncProcessorTask 
                = asyncExecutor.getAsyncTask();
        ProcessorTask processorTask = asyncProcessorTask.getProcessorTask();
        AsyncHandler asyncHandler = processorTask.getAsyncHandler();
        
        Request request = processorTask.getRequest();
        String contextPath = request.requestURI().toString();
        contextPath = contextPath.substring(contextPath.lastIndexOf("/"));  
        
        JavaMailAsyncFilterHandler handler = handlers.get(contextPath);
        if ( handler == null){
            processorTask.invokeAdapter();    
            return true;
        } 
        MailFetcher mf = mailFetcherCache.poll();
        if ( mf == null ){
            mf = new MailFetcher();
        }
        mf.handler = handler;           
        mf.processorTask = processorTask;           
        mf.asyncProcessorTask = asyncProcessorTask;           
        mf.asyncHandler = asyncHandler;           
        
        scheduler.schedule(mf,1L,TimeUnit.SECONDS);
        return false;
    }
    
    
    /**
     * Simple class that look for remote email and execute a 
     * <code>ProcessorTask</code>.
     */
    private class MailFetcher implements Runnable{
    
        /** 
         * The handler associated with this class (Servlet).
         */
        JavaMailAsyncFilterHandler handler;

        
        /**
         * The event object shared with <code>JavaMailAsyncFilterHandler</code>
         * implementation.
         */
        JavaMailAsyncFilterEvent event = new JavaMailAsyncFilterEvent();
        
    
        /**
         * The <code>ProcessorTask</code>, which is used to execture the HTTP 
         * request.
         */
        private ProcessorTask processorTask;  


        /**
         * The async wrapper around the <code>ProcessorTask</code>
         */ 
        private AsyncTask asyncProcessorTask;   


        /**
         * The Default <code>AsyncHandler</code> implementation used to execute
         * aynchronous request. This object own the main <code>Pipeline</code>
         * used to execute an asynchronous operation.
         */
        private AsyncHandler asyncHandler;        
        
    
        /**
         * If the <code>JavaMailAsyncFilterHandler</code> is ready to execute,
         * the execute it. If not, return this object to the scheduler for another
         * 10 seconds.
         */
        public void run(){   
            Message[] messages = checkMail();

            event.setMessages(messages);
            boolean continueCheckMail = handler.handleEvent(event);
            if ( !continueCheckMail ) {      
                processorTask.invokeAdapter();  
                asyncHandler.handle(asyncProcessorTask);
                mailFetcherCache.offer(this);
            } else {
                scheduler.schedule(this,10L,TimeUnit.SECONDS);
            }
        } 


        /**
         * Connect to email account and look for new emails.
         */
        private Message[] checkMail(){
            try{
                props.setProperty("mail.pop3.user", handler.getUserName());
                props.setProperty("mail.pop3.passwd", handler.getPassword());
                props.setProperty("mail.pop3.host", handler.getMailServer()); 
                props.setProperty("mail.pop3.port", handler.getMailServerPort());
                props.setProperty("mail.pop3.socketFactory.port", 
                        handler.getMailServerPort());

                URLName url = new URLName("pop3://"
                        + handler.getUserName()
                        +":"+ handler.getPassword()
                        +"@"+ handler.getMailServer()
                        +":"+ handler.getMailServerPort());

                mailSession = Session.getInstance(props, null);
                Store store = new POP3SSLStore(mailSession, url);              
                store.connect(handler.getMailServer(),
                              handler.getUserName(), handler.getPassword());

                Folder folder = store.getDefaultFolder();
                folder = folder.getFolder("INBOX");

                folder.open(Folder.READ_ONLY);

                return folder.getMessages();      
            } catch (Throwable ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }   
}
