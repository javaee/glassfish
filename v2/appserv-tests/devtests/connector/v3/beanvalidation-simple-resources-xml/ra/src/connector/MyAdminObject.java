
package connector;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.resource.spi.*;
import javax.resource.ResourceException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Set;

public class MyAdminObject implements java.io.Serializable, ResourceAdapterAssociation {

    private String resetControl="NORESET";
    private Integer expectedResults;
    private SimpleResourceAdapterImpl ra = null;

    private String email ;

    public void setEmail(String email){
       this.email = email;
    }

    @Null
    public String getEmail(){
        return email;
    }

    int intValue = -1;

    @Max(value=50)
    public int getIntValue(){
      return intValue;
    }
 
    @ConfigProperty(defaultValue="1", type=java.lang.Integer.class)
    public void setIntValue(int intValue){
      this.intValue = intValue;
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

    public boolean testRA(int intValue, Validator beanValidator){
        int originalValue = ra.getIntValue();
        ra.setIntValue(intValue);
        System.out.println("testRA : setting intValue : " + intValue);
        boolean result = testBean(ra, beanValidator, intValue);
        ra.setIntValue(originalValue);
        System.out.println("testRA : result : " + result);
        return result;
    }

    private boolean testBean(Object o, Validator beanValidator, int intValue){
        boolean validationFailure = false;
        try{
             setIntValue(intValue);
             Set violations = beanValidator.validate(o);
             if(violations!=null && violations.size() > 0){
                 validationFailure = true;
                 System.out.println("testRA : violations found");
             }else{
                 System.out.println("testRA : no violations found");
             }

        }catch(javax.validation.ConstraintViolationException cve){
          System.out.println("testRA : violations found");
          validationFailure = true;
        }catch(Exception ne){
            System.out.println("testRA : violations found -- EXCEPTION");
            validationFailure = true;
             ne.printStackTrace();
        }
        return !validationFailure;
    }

    public ResourceAdapter getResourceAdapter() {
        return ra;
    }

    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        ra = (SimpleResourceAdapterImpl)resourceAdapter;
    }
}

