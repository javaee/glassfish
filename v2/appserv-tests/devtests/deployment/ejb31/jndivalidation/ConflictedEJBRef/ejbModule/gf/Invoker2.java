package gf;

import javax.ejb.EJB;
import javax.ejb.Stateless;



@Stateless
public class Invoker2 {
	  @EJB(name="java:module/env/DuplicateEntry",beanName="ejb/myslsb2")
	  private MySLSB mySLSB;

}
