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

/**
 *
 * @author LILIZHAO
 */
@ManagedBean
@ViewScoped
public class NewJSFManagedBean implements java.io.Serializable {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    private static String requestScope = "around RequestScoped";

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public NewJSFManagedBean() {
    }
    
    public String getMessage() {
        try {
            String text = "JSF Hello World!";
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            
            if(jmsContext.toString().indexOf(requestScope) == -1){
                throw new RuntimeException("NOT in requestScope scope!");
            }
            return text;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
