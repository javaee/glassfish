package com.acme;

import javax.ejb.*;

@Stateless
public class StatelessBean {

    @EJB(beanName="MultiBean") private MultiBean multi1;
    @EJB(beanName="MultiBean2") private MultiBean multi2;
    @EJB(beanName="MultiBean3") private MultiBean multi3;

    public void foo() {

	System.out.println("In StatelessBean::foo");
		
       	String multi1Str = multi1.foo();
	String multi2Str = multi2.foo();
	String multi3Str = multi3.foo();

       	System.out.println("multi1 = " + multi1Str);
	System.out.println("multi2 = " + multi2Str);
	System.out.println("multi3 = " + multi3Str);

	if( /**!multi1Str.equals("1") || **/
	    !multi2Str.equals("2") ||
	    !multi3Str.equals("3") ) {
	    throw new EJBException("Invalid multi values");

	}
	

    }

}