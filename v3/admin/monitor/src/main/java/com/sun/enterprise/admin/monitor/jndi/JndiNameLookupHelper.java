package com.sun.enterprise.admin.monitor.jndi;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.naming.InitialContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;


public class JndiNameLookupHelper {
    private InitialContext context;
    private static final Logger logger =
            LogDomains.getLogger(JndiNameLookupHelper.class, LogDomains.ADMIN_LOGGER);
    private static final StringManager sm =
        StringManager.getManager(JndiNameLookupHelper.class);
    private final String SYSTEM_SUBCONTEXT = "__SYSTEM";

    /** Creates a new instance of JndiMBeanHelper */
    public JndiNameLookupHelper() {
        initialize();
    }

    /**
     * Initializes the JndiMBeanHelper object upon creation. It specifically
     * creates an InitialContext instance for querying the naming service
     * during certain method invocations.
     */
    void initialize() {
        try {
            context = new InitialContext();
        } catch(javax.naming.NamingException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Gets the jndi entries from the application server's naming
     * service given a particular context/subcontext.
     *
     * @param contextPath The naming context or subcontext to query.
     * @return An {@link ArrayList} of {@link javax.naming.NameClassPair} objects.
     * @throws NamingException if an error occurs when connection with
     *         the naming service is established or retrieval fails.
     */
    public ArrayList<String> getJndiEntriesByContextPath(String contextPath)
            throws NamingException {
        ArrayList<String> names;
        NamingEnumeration ee;
        if(contextPath == null) { contextPath = ""; }
        try {
            ee = context.list(contextPath);
        } catch(NameNotFoundException e) {
            String msg = sm.getString("monitor.jndi.context_notfound",
                new Object[]{contextPath});
            logger.log(Level.WARNING, msg);
            throw new NamingException(msg);
        }
        names = toNameClassPairArray(ee);
        return names;
    }

    /**
     * Changes a NamingEnumeration object into an ArrayList of
     * NameClassPair objects.
     *
     * @param ee An {@link NamingEnumeration} object to be transformed.
     * @return An {@link ArrayList} of {@link javax.naming.NameClassPair} objects.
     *
     * @throws NamingException if an error occurs when connection with
     *         the naming service is established or retrieval fails.
     */
    ArrayList<String> toNameClassPairArray(NamingEnumeration ee)
            throws javax.naming.NamingException{
        ArrayList<String> names = new ArrayList<String>();
        while(ee.hasMore()) {
            // don't add the __SYSTEM subcontext - Fix for 6041360
            Object o = ee.next();
            if(o.toString().indexOf(SYSTEM_SUBCONTEXT) == -1) {
                names.add(o.toString());
            }
        }
        return names;
    }
}
