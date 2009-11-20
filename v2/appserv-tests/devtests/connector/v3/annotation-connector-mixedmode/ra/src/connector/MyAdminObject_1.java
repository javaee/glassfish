
package connector;

import javax.resource.spi.ConfigProperty;

//@README : test to make sure that administed objects defined in ra.xml (no @AdministeredObject annotation)
// are considered for @ConfigProperty annotation

public class MyAdminObject_1 implements java.io.Serializable {

    @ConfigProperty(
            defaultValue = "NORESET",
            type = java.lang.String.class
    )
    //@README : we are setting default value to NORESET only in annotation.
    //getter (getResetControl) will throw exception if it is not NORESET
    private String resetControl;
    private Integer expectedResults;

    public void setResetControl (String value) {
        resetControl = value;
    }

    public String getResetControl () {
        if(resetControl == null || !resetControl.equals("NORESET")){
            throw new RuntimeException("reset control not initialized, should have been initialized via annotation");
        }
        return resetControl;
    }

    @ConfigProperty(
            type = java.lang.Integer.class,
            defaultValue = "88"
    )
    //@README : we are setting default value to 88 only in annotation.
    //getter (getExpectedResults) will throw exception if it is not 88
    public void setExpectedResults (Integer value) {
        expectedResults = value;
    }

    public Integer getExpectedResults () {
        if(expectedResults != 88){
            throw new RuntimeException("expected results not initialized, should have been initialized via annotation");
        }
        return expectedResults;
    }
}
