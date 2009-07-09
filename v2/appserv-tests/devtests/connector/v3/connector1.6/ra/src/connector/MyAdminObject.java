
package connector;

import javax.resource.spi.*;

public class MyAdminObject implements java.io.Serializable, ResourceAdapterAssociation {

    private String resetControl="NORESET";
    private Integer expectedResults;
    private ResourceAdapter resourceAdapter;

    public void setResetControl (String value) {
        resetControl = value;
    }

    public String getResetControl () {
        return resetControl;
    }

    public ResourceAdapter getResourceAdapter(){
        return resourceAdapter;
    }


    public void setResourceAdapter(ResourceAdapter ra) throws javax.resource.ResourceException{
        this.resourceAdapter = ra;
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
           Controls.done=false;
	   System.out.println("[MyAdminObject] Initialized the Controls to false");
	}
    }

    public boolean done() {
         return Controls.done;
    }

    public int expectedResults(){
        return Controls.expectedResults;
    }

    public Object getLockObject(){
        return Controls.readyLock;
    }

}

