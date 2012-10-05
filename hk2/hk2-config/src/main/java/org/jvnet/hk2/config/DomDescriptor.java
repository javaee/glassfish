package org.jvnet.hk2.config;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.jvnet.hk2.component.Creator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: makannan
 * Date: 4/28/12
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class DomDescriptor<T>
    extends AbstractActiveDescriptor<T> {
    /**
     * For serialization
     */
    //private static final long serialVersionUID = -9196390718074767455L;

    private Dom theDom;

    private Creator<T> creator;
    
    private T theOne;

    /**
     * For serializable
     */
    public DomDescriptor() {
        super();
    }

    /**
     * Creates the constant descriptor
     *
     * @param theDom May not be null
     * @param advertisedContracts
     * @param scope
     * @param name
     * @param qualifiers
     */
    public DomDescriptor(Dom theDom, Set<Type> advertisedContracts,
                        Class<? extends Annotation> scope, String name,
                        Set<Annotation> qualifiers) {
        super(advertisedContracts, scope, name, qualifiers, DescriptorType.CLASS, 0, null);
        super.addAdvertisedContract(ConfigBeanProxy.class.getName());
        if (theDom == null) throw new IllegalArgumentException();

        this.theDom = theDom;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getImplementation()
     */
    @Override
    public String getImplementation() {
        return theDom.getClass().getName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#getCache()
     */
    @Override
    public T getCache() {
        initTheOne();
        return theOne;
    }

    @Override
    public synchronized boolean isCacheSet() {
        return isReified();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
     */
    @Override
    public Class<?> getImplementationClass() {
        return theDom.getClass();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public T create(ServiceHandle<?> root) {
        initTheOne();
        return theOne;
    }

    private void initTheOne() {
        if (theOne == null) {
            Class c = theDom.getImplementationClass();
            creator = (ConfigBeanProxy.class.isAssignableFrom(c)
                    ? new DomProxyCreator(c, theDom.getMetadata(), theDom)
                    : new ConfiguredCreator(theDom.createCreator(c), theDom));
        }

        theOne = creator.create();
    }

    /*
    public DomDescriptor(Dom dom) {
        super(EMPTY_CONTRACT_SET, null, dom.typeName(), EMPTY_ANNOTATION_SET, DescriptorType.FACTORY, 0);

        super.addAdvertisedContract(ConfigBeanProxy.class.getName());
        super.addAdvertisedContract(ConfigBean.class.getName());
        super.addAdvertisedContract(Dom.class.getName());
        super.setImplementation(dom.typeName());
        Map<String, List<String>> metaData = dom.getMetadata();
        for (String key : metaData.keySet()) {
            List<String> values = metaData.get(key);
            if (values != null) {
                for (String value : values) {
                    super.addMetadata(key, value);
                }
            }
        }
        this.dom = dom;
        
        super.setLoader(dom.getLoader());
    }
    */

}
