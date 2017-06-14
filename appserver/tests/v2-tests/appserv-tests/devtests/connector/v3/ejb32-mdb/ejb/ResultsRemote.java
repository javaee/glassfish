package com.sun.s1asdev.ejb.ejb32.mdb.ejb;

import javax.ejb.Remote;
import java.util.List;

/**
 * @author David Blevins
 */
@Remote
public interface ResultsRemote {
    boolean awaitInvocations();
    List<String> getInvoked();
}
