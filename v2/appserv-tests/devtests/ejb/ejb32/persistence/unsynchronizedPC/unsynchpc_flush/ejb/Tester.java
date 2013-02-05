package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.unsynchpc_flush.ejb;

import javax.ejb.Remote;

@Remote
public interface Tester {
    public boolean flushBeforeJoin();
    
    public boolean flushAfterJoin();
    
    public void autoFlushByProvider(String name);
    
    public boolean isPersonFound(String name);
}
