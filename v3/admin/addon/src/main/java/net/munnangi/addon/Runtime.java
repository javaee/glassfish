package net.munnangi.addon;
import org.jvnet.hk2.annotations.CagedBy;

@CagedBy(RuntimeMBeanCageBuilder.class)
public interface Runtime {
    public String getName();
    public Object getParent();
}
