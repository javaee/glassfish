package test3;

import org.jvnet.hk2.config.*;

/**
 * Random config object with default values.
 * @author Jerome Dochez
 */
@Configured
public interface ConfigWithDefaultValues extends ConfigBeanProxy {

    @Attribute(defaultValue="text/plain,text/xml,text/plain")
    String getMimeTypes();

}
