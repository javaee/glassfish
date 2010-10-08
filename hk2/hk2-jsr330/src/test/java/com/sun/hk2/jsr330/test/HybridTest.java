package com.sun.hk2.jsr330.test;

import static org.junit.Assert.*;

import javax.inject.Named;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.junit.Hk2Runner;

import com.sun.hk2.jsr330.test.components.Aussie;
import com.sun.hk2.jsr330.test.components.HelloService;
import com.sun.hk2.jsr330.test.components.Latin;
import com.sun.hk2.jsr330.test.components.Spanish;

/**
 * Intentionally blending "proprietary" Hk2 annotations along side of
 * jsr-330 standard usage. (see the implementations of this for details).
 * 
 * Note that I don't recommend people blend a hybrid model anyway, but it
 * is good to know the details...
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
public class HybridTest {

  // injecting an hk2-style named service into a 330 injection point
  @javax.inject.Inject @Named("French") HelloService frenchHelloService;

  // injecting an hk2-style qualified service into a 330 injection point #2
  @javax.inject.Inject @Spanish HelloService spanishHelloService;
  
  // we don't speak Latin (at least not yet)
//  // injecting an hk2-style qualified service into a 330 injection point
//  @javax.inject.Inject @Latin HelloService latinHelloService;
  
  // injecting a 330-style named service into an hk2 injection point
  @org.jvnet.hk2.annotations.Inject(name="English") HelloService englishHelloService;

  // we don't like Aussie right now either
//  // injecting a 330-style qualified service into an hk2 injection point
//  @org.jvnet.hk2.annotations.Inject @Aussie HelloService aussieHelloService;
  
  @Test
  public void testItAll() {
    assertNotNull(frenchHelloService);
    assertEquals("bonjour", frenchHelloService.sayHello());

    assertNotNull(spanishHelloService);
    assertEquals("hola", spanishHelloService.sayHello());

//    assertNotNull(latinHelloService);
//    assertEquals("salve", spanishHelloService.sayHello());
    
    assertNotNull(englishHelloService);
    assertEquals("hello", englishHelloService.sayHello());
    
//    assertNotNull(aussieHelloService);
//    assertEquals("g'day", aussieHelloService.sayHello());
  }
  
}
