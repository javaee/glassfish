package connector;

import javax.resource.spi.*;

public abstract class AbstractAdminObject {

    @ConfigProperty(
            defaultValue = "NORESET",
            type = java.lang.String.class
    )
    private String resetControl;

    public void setResetControl (String value) {
        resetControl = value;
    }

    public String getResetControl () {
        return resetControl;
    }
}