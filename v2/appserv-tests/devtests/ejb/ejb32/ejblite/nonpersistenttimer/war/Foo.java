package com.sun.s1asdev.ejb32.ejblite.timer;

import javax.ejb.*;

@Local
public interface Foo extends TimerStuff {

     void remove();
}
