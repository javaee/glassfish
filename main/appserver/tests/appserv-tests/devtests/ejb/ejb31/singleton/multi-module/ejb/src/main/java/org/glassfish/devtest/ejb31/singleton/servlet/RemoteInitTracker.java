package org.glassfish.devtest.ejb31.singleton.multimodule.servlet;

import javax.ejb.Remote;
import java.util.Map;

@Remote
public interface RemoteInitTracker {

    public void  add(String entry);

    public Map<String, Integer> getInitializedNames();

}
