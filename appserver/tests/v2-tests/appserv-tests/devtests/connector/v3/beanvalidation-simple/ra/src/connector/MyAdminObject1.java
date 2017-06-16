
package connector;

import javax.validation.constraints.*;
import javax.resource.spi.*;

public class MyAdminObject1 implements java.io.Serializable {

    private String resetControl="NORESET";
    private Integer expectedResults;


    private String email ;

    public void setEmail(String email){
       this.email = email;
    }

    @Null
    public String getEmail(){
        return email;
    }

/*
    @Readme : setting a value that violates validation constraint
    This violation should be listed in the set of violated constraints (in server.log) and
    lookup of this resource must fail
 */

    int intValue = -1;
    @Max(value=50)
    public int getIntValue(){
      return intValue;
    }
 
    @ConfigProperty(type=java.lang.Integer.class)
    public void setIntValue(int intValue){
      this.intValue = intValue;
    }
/*
    @Readme : setting a value that violates validation constraint
    This violation should be listed in the set of violated constraints (in server.log) and
    lookup of this resource must fail
 */
    int intValue1 = 55;

    @Max(value=50)
    public int getIntValue1(){
      return intValue1;
    }

    @ConfigProperty(type=java.lang.Integer.class)
    public void setIntValue1(int intValue){
      this.intValue1 = intValue;
    }



    public void setResetControl (String value) {
        resetControl = value;
    }

    public String getResetControl () {
        return resetControl;
    }

    public void setExpectedResults (Integer value) {
        expectedResults = value;
    }

    public Integer getExpectedResults () {
        return expectedResults;
    }

    public void initialize() {
	System.out.println("[MyAdminObject] Initializing the Controls to false:"+resetControl);
        if (resetControl.equals("BEGINNING")) {
            synchronized (Controls.readyLock){
                Controls.done=false;
            }
	   System.out.println("[MyAdminObject] Initialized the Controls to false");
	}
    }

    public boolean done() {
        synchronized (Controls.readyLock){
         return Controls.done;
        }
    }

    public int expectedResults(){
        synchronized (Controls.readyLock){

        return Controls.expectedResults;
        }
    }

    public Object getLockObject(){
        return Controls.readyLock;
    }

}

