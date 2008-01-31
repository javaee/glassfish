package org.jvnet.hk2.component;

/**
 *
 * A resource that can be injected into a component might optinally want to be notified of such
 * injection. This can be useful to track usage or to set up a notification mechanim
 * for change happening in the injected resource.
 *
 * @author Jerome Dochez
 */
public interface Injectable {

    /**
     * notification of injection into a component
     * @param target the component in which we are injected.
     */
    public void injectedInto(Object target);
}
