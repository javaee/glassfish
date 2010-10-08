package bindings;

import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.accessories.SpareTire;
import org.jvnet.hk2.annotations.Service;

import com.sun.hk2.jsr330.BasicBinding;
import com.sun.hk2.jsr330.Jsr330Binding;

/**
 * Single binding example.
 * 
 * @author Jeff Trent
 */
@Service
public class TckTireInjectorBinding extends BasicBinding implements Jsr330Binding {

  public TckTireInjectorBinding() {
    setServiceClass(SpareTire.class);
    addContractClass(Tire.class);
    addName("spare");
  }
  
}
