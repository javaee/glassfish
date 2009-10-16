package org.glassfish.devtest.ejb31.singleton.servlet;

import javax.ejb.Remote;
import java.util.List;

@Remote
public interface RemoteInitTracker {

    public void  add(String entry);

    public List getInitializedNames();

}
