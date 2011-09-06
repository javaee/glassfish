package org.glassfish.dcom;


import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.*;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.*;

/**
 *
 * @author bnevins
 */
public class Scripter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            args = new String[] { "oracle", "oracle", "wnevins", "Bolongo23Nasal"};
             new Scripter().foo(args);
        }
        catch (Exception ex) {
            Logger.getLogger(Scripter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void foo(String[] args) throws JIException, UnknownHostException {
// Create a session
        JISession session = JISession.createSession(args[1], args[2], args[3]);
        session.useSessionSecurity(true);

// Execute command
        JIComServer comStub = new JIComServer(JIProgId.valueOf("WScript.Shell"),"10.28.51.117", session);
        IJIComObject unknown = comStub.createInstance();
        IJIComObject comobject = unknown.queryInterface(IJIDispatch.IID);
        IJIDispatch shell = (IJIDispatch)JIObjectFactory.narrowObject(comobject);

        Object[] scriptArgs = new Object[] {
            new JIString("%comspec% /c d:/glassfish3/bin/asadmin.bat stop-domain" )
        };

        JIVariant results[] = shell.callMethodA("Exec", scriptArgs);
    }
}
/*
        //JIVariant stdOutJIVariant = shell.get("StdOut");
    //IJIDispatch stdOut =  (IJIDispatch)JIObjectFactory.narrowObject(stdOutJIVariant.getObjectAsComObject());

while(!((JIVariant)stdOut.get("AtEndOfStream")).getObjectAsBoolean()){
    System.out.println(stdOut.callMethodA("ReadAll").getObjectAsString().getString());
}


     IJIComObject resultsObject = results[0].getObjectAsComObject();
        	IJIDispatch resultsDispatchObject = (IJIDispatch)JIObjectFactory.narrowObject(resultsObject);
		results = wbemServices_dispatch.callMethodA("InstancesOf", new Object[]{new JIString("Win32_Process"), new Integer(0), JIVariant.OPTIONAL_PARAM()});
		IJIDispatch wbemObjectSet_dispatch = (IJIDispatch)JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());
		JIVariant variant = wbemObjectSet_dispatch.get("_NewEnum");
		IJIComObject object2 = variant.getObjectAsComObject();


 */
