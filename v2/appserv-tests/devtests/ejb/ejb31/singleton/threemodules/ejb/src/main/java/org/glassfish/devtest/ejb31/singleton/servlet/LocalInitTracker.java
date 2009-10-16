package org.glassfish.devtest.ejb31.singleton.servlet;

import javax.ejb.Local;
import java.util.List;


@Local
public interface LocalInitTracker {
    
    public void add(String entry);

    public List getInitializedNames();
}
