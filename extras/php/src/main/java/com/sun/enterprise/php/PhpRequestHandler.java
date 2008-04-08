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

package com.sun.enterprise.php;

import org.jvnet.glassfish.api.content.FileServer;
import org.jvnet.glassfish.api.content.WebRequestHandler;
import com.sun.enterprise.web.connector.grizzly.standalone.DynamicContentAdapter;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import com.caucho.quercus.Quercus;
import com.caucho.quercus.QuercusDieException;
import com.caucho.quercus.QuercusExitException;
import com.caucho.quercus.QuercusLineRuntimeException;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.QuercusValueException;
import com.caucho.quercus.module.QuercusModule;
import com.caucho.quercus.page.QuercusPage;

import org.apache.coyote.Request;
import org.apache.coyote.Response;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Vector;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.caucho.vfs.*;
import org.apache.coyote.http11.InternalOutputBuffer;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.http.MimeHeaders;

/**
 * Class that listens to particulr web request pattern ending with .php and
 * serve the associated file using the Quercus engine
 *
 * @author Jerome Dochez
 * 
 */
@WebRequestHandler(urlPattern=".*php$")
@Service
public class PhpRequestHandler extends DynamicContentAdapter implements FileServer, PostConstruct {

    Logger log = Logger.getAnonymousLogger();
    
    Quercus quercus;

    public PhpRequestHandler() {
        super(".");
    }
    public PhpRequestHandler(String publicDirectory) {
        super(publicDirectory);
    }
    /**
     * Initialize the handler for the first invocation. This is
     * done lazily by the application server when the first
     * request satisfying the pattern need to be serviced.
     *
     * @return true if we are ready to service requests.
     */
    public void postConstruct() {
        
        System.out.println("postConstruct invoked");
        ClassLoader cll = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader quercusCL = Quercus.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(quercusCL);
            quercus = new Quercus();

        } finally {
            Thread.currentThread().setContextClassLoader(cll);
        }
    }

    protected int getTokenID() {
        return 18;
    }

    public void service(Request req, Response res) throws Exception {
        MessageBytes mb = req.requestURI();
        ByteChunk requestURI = mb.getByteChunk();

        try{
            String uri = requestURI.toString();
            if (contextRoot!=null && requestURI.startsWith(contextRoot)) {
                uri = uri.substring(contextRoot.length());
            }
            File file = new File(getRootFolder(),uri);
            if (file.isDirectory()) {
                uri += "index.html";
                file = new File(file,uri);
            }

            if (file.canRead()) {
                if (uri.endsWith(".php")) {
                    serviceDynamicContent(req, res);
                } else {
                    super.service(uri, req, res);
                }
            }

            res.finish();
        } catch (Exception e) {
            if (SelectorThread.logger().isLoggable(Level.SEVERE)) {
                SelectorThread.logger().log(Level.SEVERE, e.getMessage());
            }

            throw e;
        }
    }      
    /**
     * Service a particular URL request.
     *
     * @param request  the URL request
     * @param response the response to use to send back content
     *                 to the client
     */
    public void serviceDynamicContent(Request request, Response response) {
        System.out.println("PHP invoked");

        File file = new File(getRootFolder(), request.requestURI().getString());
        if (file.isDirectory()) {
            file = new File(file, "index.html");
        }

        DynamicContentAdapter.RailsToken rt = (DynamicContentAdapter.RailsToken)request.getNote(getTokenID());
        if (rt == null){
            rt = new DynamicContentAdapter.RailsToken();
        }
        rt.req = request;        
        try {
            if (quercus == null) {
                response.setStatus(503);
                return;
            }

            request.doRead(rt.readChunk);
            ((InternalOutputBuffer)response.getOutputBuffer()).commit();
            response.setCommitted(true);


            System.out.println(request.requestURI());
            OutputStream os =
                ((InternalOutputBuffer)response.getOutputBuffer()).getOutputStream();
            


            Path path = new FilePath(file.getAbsolutePath());
            QuercusPage page = quercus.parse(path);


            WriteStream ws;
            HttpServletRequest newRequest = (HttpServletRequest) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[] { HttpServletRequest.class }, new PhpRequest(request));

            HttpServletResponse newResponse = (HttpServletResponse) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[] { HttpServletResponse.class }, new RequestInvocationHandler(response));

            VfsStream s = new VfsStream(null, os);
            ws = new WriteStream(s);

            Env env = new Env(quercus, page, ws, newRequest, newResponse);
            try {
                env.setGlobalValue("request", env.wrapJava(newRequest));
                env.setGlobalValue("response", env.wrapJava(newResponse));

                env.start();

                String prepend = env.getIniString("auto_prepend_file");
                if (prepend != null) {
                    QuercusPage prependPage = quercus.parse(env.lookup(prepend));
                    prependPage.executeTop(env);
                }

                page.executeTop(env);

                String append = env.getIniString("auto_append_file");
                if (append != null) {
                    QuercusPage appendPage = quercus.parse(env.lookup(append));
                    appendPage.executeTop(env);
                }
                //   return;
            } catch (QuercusExitException e) {
                e.printStackTrace();
                log.severe(e.getMessage());
                throw e;
            } catch (QuercusLineRuntimeException e) {
                e.printStackTrace();
                log.log(Level.FINE, e.toString(), e);

                //  return;
            } catch (QuercusValueException e) {
                e.printStackTrace();
                log.log(Level.FINE, e.toString(), e);

                ws.println(e.toString());

                //  return;
            } catch (Throwable e) {
                e.printStackTrace();
                if (response.isCommitted())
                    e.printStackTrace(ws.getPrintWriter());

                ws = null;

                throw e;
            } finally {
                env.close();

                // don't want a flush for an exception
                if (ws != null)
                    ws.close();

                rt.recycle();
                request.setNote(getTokenID(),rt);

                response.finish();

            }
        } catch (QuercusDieException e) {
            e.printStackTrace();
            log.log(Level.FINE, e.toString(), e);
            // normal exit
        } catch (QuercusExitException e) {
            e.printStackTrace();
            log.log(Level.FINER, e.toString(), e);
            // normal exit
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private class RequestInvocationHandler implements InvocationHandler {

        Object target;

        RequestInvocationHandler(Object target) {
            this.target = target;
        }

        /**
         * Processes a method invocation on a proxy instance and returns
         * the result.  This method will be invoked on an invocation handler
         * when a method is invoked on a proxy instance that it is
         * associated with.
         *
         * @param    proxy the proxy instance that the method was invoked on
         * @param    method the <code>Method</code> instance corresponding to
         * the interface method invoked on the proxy instance.  The declaring
         * class of the <code>Method</code> object will be the interface that
         * the method was declared in, which may be a superinterface of the
         * proxy interface that the proxy class inherits the method through.
         * @param    args an array of objects containing the values of the
         * arguments passed in the method invocation on the proxy instance,
         * or <code>null</code> if interface method takes no arguments.
         * Arguments of primitive types are wrapped in instances of the
         * appropriate primitive wrapper class, such as
         * <code>java.lang.Integer</code> or <code>java.lang.Boolean</code>.
         * @return the value to return from the method invocation on the
         * proxy instance.  If the declared return type of the interface
         * method is a primitive type, then the value returned by
         * this method must be an instance of the corresponding primitive
         * wrapper class; otherwise, it must be a type assignable to the
         * declared return type.  If the value returned by this method is
         * <code>null</code> and the interface method's return type is
         * primitive, then a <code>NullPointerException</code> will be
         * thrown by the method invocation on the proxy instance.  If the
         * value returned by this method is otherwise not compatible with
         * the interface method's declared return type as described above,
         * a <code>ClassCastException</code> will be thrown by the method
         * invocation on the proxy instance.
         * @throws Throwable the exception to throw from the method
         * invocation on the proxy instance.  The exception's type must be
         * assignable either to any of the exception types declared in the
         * <code>throws</code> clause of the interface method or to the
         * unchecked exception types <code>java.lang.RuntimeException</code>
         * or <code>java.lang.Error</code>.  If a checked exception is
         * thrown by this method that is not assignable to any of the
         * exception types declared in the <code>throws</code> clause of
         * the interface method, then an
         * {@link java.lang.reflect.UndeclaredThrowableException} containing the
         * exception that was thrown by this method will be thrown by the
         * method invocation on the proxy instance.
         * @see    java.lang.reflect.UndeclaredThrowableException
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method reqMethod = null;
            try {
                reqMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            } catch(NoSuchMethodException e) {
                // do nothing
            }
            if (reqMethod==null) {
                if (method.getName().startsWith("get")) {
                    String name = String.valueOf(method.getName().charAt(3)).toLowerCase() + method.getName().substring(4);
                    try {
                        reqMethod = target.getClass().getMethod(name, method.getParameterTypes());
                    } catch(NoSuchMethodException e) {
                        // do nothing
                    }
                } else
                if (method.getName().startsWith("set")) {
                    String name = String.valueOf(method.getName().charAt(3)).toLowerCase() + method.getName().substring(4);
                    try {
                        reqMethod = target.getClass().getMethod(name, method.getParameterTypes());
                    } catch(NoSuchMethodException e) {
                        // do nothing
                    }
                        
                }
            }
            if (reqMethod!=null) {
                Object returnValue = reqMethod.invoke(target, args);
                if (returnValue instanceof  org.apache.tomcat.util.buf.MessageBytes) {
                    return returnValue.toString();
                }
                return returnValue;
            }
            if (method.getName().equals("getRealPath")) {
                return getRootFolder() + args[0];
            }
            if (method.getName().equals("getContextPath")) {
                return "/wiki/";
            }
            if (method.getName().equals("getServletPath")) {
                return "";
            }
            if (method.getName().equals("isSecure")) {
                return Boolean.FALSE;
            }
            if (method.getName().equals("getHeaderNames")) {
                Request request = (Request) target;
                MimeHeaders headers = request.getMimeHeaders();
                Vector<String> names = new Vector();
                for (int i=0;i<headers.size();i++) {
                    names.add(headers.getName(i).toString());    
                }
                return names.elements();
                
            }
            System.out.println("Cannot satisfy method " + method);
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
