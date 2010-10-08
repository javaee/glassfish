package bindings;

import java.util.List;

import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;
import org.jvnet.hk2.annotations.Service;

import com.sun.hk2.jsr330.BasicBinding;
import com.sun.hk2.jsr330.BindingFactory;
import com.sun.hk2.jsr330.Jsr330Binding;
import com.sun.hk2.jsr330.Jsr330Bindings;

/**
 * Testing producer-type bindings.
 * 
 * @author Jeff Trent
 */
@Service
public class TckInjectorBindings2 implements Jsr330Bindings {

  @Override
  public void getBindings(List<Jsr330Binding> bindings, BindingFactory factory) {
    bindings.add(cupHolder());
    bindings.add(spareTire());
    bindings.add(fuelTank());
  }

  public static Jsr330Binding cupHolder() {
    return new BasicBinding(Cupholder.class);
  }

  public static Jsr330Binding spareTire() {
    return new BasicBinding(SpareTire.class);
  }

  public static Jsr330Binding fuelTank() {
    // use the name instead
    return new BasicBinding(FuelTank.class.getCanonicalName());
  }

}
