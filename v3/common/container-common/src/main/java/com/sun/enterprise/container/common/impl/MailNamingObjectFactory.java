package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.deployment.MailConfiguration;
import com.sun.enterprise.naming.spi.NamingObjectFactory;
import com.sun.enterprise.naming.spi.NamingUtils;
import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.PrintStream;

@Service
public class MailNamingObjectFactory
    implements NamingObjectFactory {

    private String name;

    private String physicalJndiName;

    private NamingUtils namingUtils;
    
    public MailNamingObjectFactory(String name, String physicalJndiName,
                                    NamingUtils namingUtils) {
        this.name = name;
        this.physicalJndiName = physicalJndiName;

        this.namingUtils = namingUtils;
    }

    public boolean isCreateResultCacheable() {
        return false;
    }

    public Object create(Context ic)
        throws NamingException {
		MailConfiguration config =
		    (MailConfiguration) ic.lookup(physicalJndiName);

		// Note: javax.mail.Session is not serializable,
		// but we need to get a new instance on every lookup.
		javax.mail.Session s = javax.mail.Session.getInstance(
								      config.getMailProperties(), null);
		s.setDebugOut(new PrintStream(namingUtils.getMailLogOutputStream()));
		s.setDebug(true);

        return s;
    }
}
