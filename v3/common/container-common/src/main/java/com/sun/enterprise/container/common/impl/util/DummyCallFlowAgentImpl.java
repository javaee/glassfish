package com.sun.enterprise.container.common.impl.util;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;
import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import com.sun.enterprise.container.common.spi.util.EntityManagerMethod;
import com.sun.enterprise.container.common.spi.util.EntityManagerQueryMethod;

@Service
@Scoped(Singleton.class)
public class DummyCallFlowAgentImpl
    implements CallFlowAgent {

    public boolean isEnabled() {return false;}

    public void entityManagerMethodStart(EntityManagerMethod val) {}

    public void entityManagerMethodEnd() {}

    public void entityManagerQueryStart(EntityManagerQueryMethod val) {}

    public void entityManagerQueryEnd() {}
}
