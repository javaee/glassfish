/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.jms.test;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.ejb.*;
import javax.transaction.UserTransaction;

/**
 *
 * @author LILIZHAO
 */
@ManagedBean
@ViewScoped
@TransactionManagement(TransactionManagementType.BEAN)
public class NewJSFManagedBean implements java.io.Serializable {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Inject 
    UserTransaction ut;

    private static String transactionScope = "around TransactionScoped";

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public NewJSFManagedBean() {
    }
    
    public String getMessage() {
        String context = "";
        try {
            String text = "JSF Hello World!";
            ut.begin();
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            context = jmsContext.toString();
            ut.commit();
            if(context.indexOf(transactionScope) == -1){
                throw new RuntimeException("NOT in transaction scope!");
            }
            return text;
        } catch (Exception e) {
            try {                
                ut.rollback();
            } catch (Exception ex) {
                e.printStackTrace();
            } 
            throw new RuntimeException(e);
        }
    }
}
