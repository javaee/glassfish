package contractBy;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.ContractProvided;

@Service
@ContractProvided(Bar.class)
public class BarImpl implements Bar {
    public void doit() {}
}
