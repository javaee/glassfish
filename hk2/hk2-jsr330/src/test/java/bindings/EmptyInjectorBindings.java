package bindings;

import java.util.List;

import org.jvnet.hk2.annotations.Service;

import com.sun.hk2.jsr330.BindingFactory;
import com.sun.hk2.jsr330.Jsr330Binding;
import com.sun.hk2.jsr330.Jsr330Bindings;

/**
 * Testing the robustness of the impl.
 * 
 * @author Jeff Trent
 */
@Service
public class EmptyInjectorBindings implements Jsr330Bindings {

  @Override
  public void getBindings(List<Jsr330Binding> list, BindingFactory factory) {
    // TODO Auto-generated method stub
    
  }

}
