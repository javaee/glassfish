package com.sun.hk2.jsr330.test.components;

import java.util.List;

import org.jvnet.hk2.annotations.Service;

import com.sun.hk2.jsr330.BindingFactory;
import com.sun.hk2.jsr330.Jsr330Binding;
import com.sun.hk2.jsr330.Jsr330Bindings;
import com.sun.hk2.jsr330.test.HybridTest;

/**
 * Service bindings for the {@link HybridTest}
 * 
 * @author Jeff Trent
 */
@Service
public class Bindings implements Jsr330Bindings {

  @Override
  public void getBindings(List<Jsr330Binding> list,
      BindingFactory factory) {
    list.add(aussie(factory));
    list.add(english(factory));
  }

  private Jsr330Binding english(BindingFactory factory) {
    return factory.newBinding(EnglishHelloService.class).addContractClass(HelloService.class).addName("English");
  }

  private Jsr330Binding aussie(BindingFactory factory) {
    return factory.newBinding(AussieHelloService.class).addContractClass(HelloService.class).addQualifier(Aussie.class);
  }

}
