
package connector;

public class MyAdminObject implements java.io.Serializable {

    private String resetControl="NORESET";
    private Integer expectedResults;

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

