package com.sun.enterprise.container.common.spi.util;

import org.jvnet.hk2.annotations.Contract;
import com.sun.enterprise.container.common.spi.util.EntityManagerMethod;
import com.sun.enterprise.container.common.spi.util.EntityManagerQueryMethod;

@Contract
public interface CallFlowAgent {

    public boolean isEnabled();

    public void entityManagerMethodStart(EntityManagerMethod val);

    public void entityManagerMethodEnd();

    public void entityManagerQueryStart(EntityManagerQueryMethod val);

    public void entityManagerQueryEnd();
    
}
