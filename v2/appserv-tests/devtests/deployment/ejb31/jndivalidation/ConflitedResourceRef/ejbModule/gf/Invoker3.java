package gf;

import javax.annotation.Resource;
import javax.ejb.Stateless;



@Stateless
public class Invoker3 {

	  @Resource(name="java:module/env/DupResourceRef",lookup="jdbc/__default") 
	  private javax.sql.DataSource customerAppDB;
	  
}
