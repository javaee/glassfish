package gf;

import javax.ejb.EJB;
import javax.ejb.Stateless;



@Stateless
public class Invoker1 {
	  @EJB(name="java:module/env/DuplicateEntry",beanName="ejb/myslsb")
	  private MySLSB mySLSB;

}
