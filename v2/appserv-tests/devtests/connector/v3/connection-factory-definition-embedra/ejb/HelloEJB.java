package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateless;

import javax.naming.InitialContext;
import javax.resource.ConnectionFactoryDefinitions;
import javax.resource.ConnectionFactoryDefinition;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.annotation.Resource;

@ConnectionFactoryDefinitions(
     value = {
          @ConnectionFactoryDefinition(
                description="global-scope resource defined by @ConnectionFactoryDefinition",
                name = "java:global/env/HelloEJB_ModByDD_ConnectionFactory",
                className = "javax.resource.cci.ConnectionFactory",
                resourceAdapter = "#cfd-ra",
                properties = {"testName=foo"}
          ),
          @ConnectionFactoryDefinition(
               description = "global-scope resource defined by @ConnectionFactoryDefinition", 
               name = "java:global/env/HelloEJB_Annotation_ConnectionFactory", 
               className = "javax.resource.cci.ConnectionFactory", 
               resourceAdapter = "#cfd-ra",
               transactionSupport = TransactionSupportLevel.LocalTransaction,
               maxPoolSize = 16,
               minPoolSize = 4,
               properties = {"testName=foo"}
          ),
          
          @ConnectionFactoryDefinition(
               description = "application-scope resource defined by @ConnectionFactoryDefinition", 
               name = "java:app/env/HelloEJB_Annotation_ConnectionFactory", 
               className = "javax.resource.cci.ConnectionFactory", 
               transactionSupport = TransactionSupportLevel.XATransaction,
               maxPoolSize = 16,
               minPoolSize = 4,
               resourceAdapter = "#cfd-ra",
               properties = {"testName=foo"}
          ),
          
          @ConnectionFactoryDefinition(
               description = "module-scope resource defined by @ConnectionFactoryDefinition", 
               name = "java:module/env/HelloEJB_Annotation_ConnectionFactory", 
               className = "javax.resource.cci.ConnectionFactory", 
               resourceAdapter = "#cfd-ra",
               properties = {"testName=foo"}
          ),
          
          @ConnectionFactoryDefinition(
               description = "component-scope resource defined by @ConnectionFactoryDefinition", 
               name = "java:comp/env/HelloEJB_Annotation_ConnectionFactory", 
               className = "javax.resource.cci.ConnectionFactory", 
               resourceAdapter = "#cfd-ra",
               properties = {"testName=foo"}
          )

     }
)
@Stateless
public class HelloEJB implements Hello {

    @javax.annotation.Resource(name="java:comp/env/HelloEJB_Annotation_ConnectionFactory")
    ConnectionFactory cf;
    
    public void hello() {
        try {
            Connection c = cf.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to access connector resource through injection", e);
        }

        // Connection-Factory-Definition through Annotation
        lookupConnectionFactory("java:global/env/Servlet_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/Servlet_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/Servlet_ConnectionFactory", false);
        lookupConnectionFactory("java:comp/env/Servlet_ConnectionFactory", false);

        lookupConnectionFactory("java:global/env/HelloStatefulEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/HelloStatefulEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/HelloStatefulEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:comp/env/HelloStatefulEJB_Annotation_ConnectionFactory", false);

        lookupConnectionFactory("java:global/env/HelloEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/HelloEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/HelloEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:comp/env/HelloEJB_Annotation_ConnectionFactory", true);

        // Connection-Factory-Definition through DD
        lookupConnectionFactory("java:global/env/EAR_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/EAR_ConnectionFactory", true);

        lookupConnectionFactory("java:global/env/Web_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/Web_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/Web_DD_ConnectionFactory", false);
        lookupConnectionFactory("java:comp/env/Web_DD_ConnectionFactory", false);

        lookupConnectionFactory("java:global/env/HelloStatefulEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/HelloStatefulEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/HelloStatefulEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:comp/env/HelloStatefulEJB_DD_ConnectionFactory", false);

        lookupConnectionFactory("java:global/env/HelloEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/HelloEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/HelloEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:comp/env/HelloEJB_DD_ConnectionFactory", true);
        
        System.out.println("In HelloEJB::hello()");
    }

    private void lookupConnectionFactory(String jndiName, boolean expectSuccess) throws RuntimeException{
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            ConnectionFactory ds = (ConnectionFactory) ic.lookup(jndiName);
            c = ds.getConnection();
            System.out.println("Stateless EJB: can access connector resource : " + jndiName);
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
                throw new RuntimeException("Fail to access connector resource: "+jndiName, e);
            }else{
                System.out.println("Stateless EJB cannot access connector resource : " + jndiName);
            }
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
            }
        }
    }
    

}
