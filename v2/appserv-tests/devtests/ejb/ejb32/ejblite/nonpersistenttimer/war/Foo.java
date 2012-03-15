package com.sun.s1asdev.ejb31.ejblite.timer;

import javax.ejb.*;

@Local
public interface Foo extends TimerStuff {

     void remove();
}
