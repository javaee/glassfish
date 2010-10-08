package bindings;

import java.util.List;

import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
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
public class TckInjectorBindings1 implements Jsr330Bindings {

  @Override
  public void getBindings(List<Jsr330Binding> bindings, BindingFactory factory) {
    bindings.add(car());
    bindings.add(driversSet());
    bindings.add(seat());
    bindings.add(tire());
    bindings.add(v8Engine());
  }
  
  public static Jsr330Binding car() {
    BasicBinding binding = new BasicBinding();
    binding.setServiceClass(Convertible.class).addContractClass(Car.class);
    return binding;
  }

  public static Jsr330Binding driversSet() {
    BasicBinding binding = new BasicBinding();
    binding.setServiceClass(DriversSeat.class).addContractClass(Seat.class);
    binding.addQualifier(Drivers.class);
    return binding;
  }

  public static Jsr330Binding seat() {
    return new BasicBinding(Seat.class);
  }

  public static Jsr330Binding tire() {
    return new BasicBinding(Tire.class);
  }

  public static Jsr330Binding v8Engine() {
    BasicBinding binding = new BasicBinding();
    binding.setServiceClass(V8Engine.class).addContractClass(Engine.class);
    return binding;
  }

}
