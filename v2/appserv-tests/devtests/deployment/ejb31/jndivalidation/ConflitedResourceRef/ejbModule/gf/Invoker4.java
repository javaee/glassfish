package gf;

import javax.annotation.Resource;
import javax.ejb.Stateless;



@Stateless
public class Invoker4 {
	@Resource(name="java:module/env/DupResourceRef",lookup="jdbc/__TimerPool") 
	  private javax.sql.DataSource customerAppDB;
 

}
