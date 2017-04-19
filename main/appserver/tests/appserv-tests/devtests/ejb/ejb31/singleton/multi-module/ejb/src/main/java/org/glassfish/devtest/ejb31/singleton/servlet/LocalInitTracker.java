package org.glassfish.devtest.ejb31.singleton.multimodule.servlet;

import javax.ejb.Local;
import java.util.Map;


@Local
public interface LocalInitTracker {
    
    public void add(String entry);

    public Map<String, Integer> getInitializedNames();
}
