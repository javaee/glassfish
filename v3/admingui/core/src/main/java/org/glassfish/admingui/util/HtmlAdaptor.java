/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.util;

/**
 *
 * @author anilam
 */
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class HtmlAdaptor {

    private static boolean _bHtmlAdaptorServerRegistered = false;

    public static void registerHTMLAdaptor(MBeanServer mbs) {
        if(_bHtmlAdaptorServerRegistered)
            return;
        try {
            int port = Integer.parseInt(System.getProperty("html.adaptor.port", "4444"));
            Class cl =  Class.forName("com.sun.jdmk.comm.HtmlAdaptorServer");
            Constructor contr = cl.getConstructor(new Class[]{Integer.TYPE});
            Object adaptor = contr.newInstance(new Object[]{Integer.valueOf(port)});
            Method method = cl.getMethod("start");
            ObjectName htmlAdaptorObjectName = new ObjectName(
                    "Adaptor:name=html,port="+port);
            mbs.registerMBean(adaptor, htmlAdaptorObjectName);
            method.invoke(adaptor);
            _bHtmlAdaptorServerRegistered = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
