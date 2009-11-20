
package connector;

import javax.resource.spi.AdministeredObject;
import javax.resource.spi.ConfigProperty;

//@README : test to make sure that only one admin object is created (an admin object Interface_2 & MyAdminObject_5
// should not be created)

@AdministeredObject(
        adminObjectInterfaces = {connector.MyAdminObject_Interface_3.class
                                 }
)
public class MyAdminObject_5 implements java.io.Serializable, MyAdminObject_Interface_2 {

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
        if(resetControl == null || !resetControl.equals("NORESET")){
            throw new RuntimeException("reset control not initialized, should have been initialized via annotation");
        }
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
        if(expectedResults != 88){
            throw new RuntimeException("expected results not initialized, should have been initialized via annotation");
        }
        return expectedResults;
    }
}
