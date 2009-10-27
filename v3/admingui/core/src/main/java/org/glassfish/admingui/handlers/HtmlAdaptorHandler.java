/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.handlers;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import org.glassfish.admingui.common.util.V3AMX;
/**
 *
 * @author anilam
 */
public class HtmlAdaptorHandler {

    /**
     * <p> This handler will be called only when user open version window in order to mimize impact to the rest of program.
     * Also need to define html.adaptor.port for the port number.
     */
    @Handler(id="initHtmlAdaptor")
    public static void initHtmlAdaptor(HandlerContext handlerCtx){

        if ( ! _bHtmlAdaptorServerRegistered ){
            if (System.getProperty("html.adaptor.port") == null){
                _bHtmlAdaptorServerRegistered = true;
            }
            registerHTMLAdaptor(V3AMX.getInstance().getMbeanServerConnection());
        }
    }

    private static boolean _bHtmlAdaptorServerRegistered = false;

    private static void registerHTMLAdaptor(MBeanServerConnection mbsc) {
        try {
            int port = Integer.parseInt(System.getProperty("html.adaptor.port", "4444"));
            Class cl =  Class.forName("com.sun.jdmk.comm.HtmlAdaptorServer");
            Constructor contr = cl.getConstructor(new Class[]{Integer.TYPE});
            Object adaptor = contr.newInstance(new Object[]{Integer.valueOf(port)});
            Method method = cl.getMethod("start");
            ObjectName htmlAdaptorObjectName = new ObjectName(
                    "Adaptor:name=html,port="+port);
            MBeanServer mbs = (MBeanServer) mbsc;
            mbs.registerMBean(adaptor, htmlAdaptorObjectName);
            method.invoke(adaptor);
            _bHtmlAdaptorServerRegistered = true;
        } catch (Exception e) {
            System.out.println("Warning !! cannot create HTML Adapter. Ensure that you have jmxtools.jar in __adnmingui/WEB-INF/lib directory");
            System.out.println(e.getMessage());
        }
    }

}
