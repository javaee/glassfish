
package connector;

import javax.resource.spi.AdministeredObject;
import javax.resource.spi.ConfigProperty;

@AdministeredObject(
        adminObjectInterfaces = {connector.MyAdminObject.class}
)
public class MyAdminObject implements java.io.Serializable {

    @ConfigProperty(
            defaultValue = "NORESET",
            type = java.lang.String.class
    )
    private String resetControl;
    private Integer expectedResults;

    public void setResetControl (String value) {
        resetControl = value;
    }

    public String getResetControl () {
        return resetControl;
    }

    @ConfigProperty(
            type = java.lang.Integer.class,
            defaultValue = "88"
    )
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

