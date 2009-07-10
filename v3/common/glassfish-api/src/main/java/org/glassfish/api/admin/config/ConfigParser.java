package org.glassfish.api.admin.config;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.Habitat;

import java.net.URL;
import java.io.IOException;

/**
 * @author Jerome Dochez
 * @author Vivek Pandey
 */
@Contract
public interface ConfigParser {

    /**
     * Parse a Container configuration and add it the main configuration.
     *
     * @param habitat habitat were to
     * @param configuration
     * @deprecated This method needed to be generic and should not be used. Use {@link #parseContainerConfig(org.jvnet.hk2.component.Habitat,java.net.URL, Class)}.
     */
    @Deprecated
    public Container parseContainerConfig(Habitat habitat, URL configuration) throws IOException;


    /**
     * Parse a Container's configuration defined by it's XML template pointed by configuration URL.
     * <br/> <br/>
     * Example:<br/>
     *
     * Inside your {@link org.glassfish.api.container.Sniffer}:
     *
     * <pre>
     *
     * {@link @Inject}
     * ConfigParser parser;
     *
     * {@link @Inject}
     * JrubyContainer container;
     *
     * public Module[] setup(java.lang.String s, java.util.logging.Logger logger) throws java.io.IOException{
     *     if(container == null){
     *         URL xml = getClass().getClassLoader().getResource("jruby-container-config.xml");
     *         config = parser.parseContainerConfig(habitat, xml, JrubyContainer.class);
     *         //Now do stuff with config
     *     }
     * }
     * </pre>
     * 
     * @return Confgured container
     * @throws IOException
     */
    public <T extends Container> T parseContainerConfig(Habitat habitat, URL configuration, Class<T> containerType) throws IOException;

}
