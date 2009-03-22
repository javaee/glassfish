package com.acme;

import javax.ejb.*;

@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class Super {

       @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
       public void hello3() {}

       public void hello4() {}

}