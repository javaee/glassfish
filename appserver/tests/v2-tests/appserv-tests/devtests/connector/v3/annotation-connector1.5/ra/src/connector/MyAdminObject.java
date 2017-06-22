
package connector;

import javax.resource.spi.AdministeredObject;
import javax.resource.spi.ConfigProperty;

@AdministeredObject(
        adminObjectInterfaces = {connector.MyAdminObject.class}
)
public class MyAdminObject extends AbstractAdminObject implements java.io.Serializable {

    private Integer expectedResults;

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
	System.out.println("[MyAdminObject] Initializing the Controls to false:"+getResetControl());
        if (getResetControl().equals("BEGINNING")) {
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

