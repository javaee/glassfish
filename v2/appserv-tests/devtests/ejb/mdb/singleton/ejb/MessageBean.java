package com.sun.s1asdev.ejb.mdb.singleton;

import javax.jms.*;
import javax.ejb.*;

public class MessageBean implements  MessageListener {
    @EJB
    private StatusBean statusBean;

    public void onMessage(Message message) {
        statusBean.addMessageBeanInstance(this.toString());
    }
}
