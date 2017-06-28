package org.glassfish.jaccApi.programmaticauthentication.sam;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import org.glassfish.jaccApi.common.BaseServletContextListener;
import org.glassfish.jaccApi.common.JaspicUtils;

/**
 * 
 * @author Arjan Tijms
 * 
 */
@WebListener
public class SamAutoRegistrationListener extends BaseServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JaspicUtils.registerSAM(sce.getServletContext(), new TestServerAuthModule());
    }

}