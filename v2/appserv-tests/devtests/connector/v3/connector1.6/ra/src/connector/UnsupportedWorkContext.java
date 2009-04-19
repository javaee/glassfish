package connector;

import javax.resource.spi.work.WorkContext;

public class UnsupportedWorkContext implements WorkContext {
    public String getName() {
        return "unsupportedWorkContext";
    }

    public String getDescription() {
        return "unsupportedWorkContext";
    }
}
