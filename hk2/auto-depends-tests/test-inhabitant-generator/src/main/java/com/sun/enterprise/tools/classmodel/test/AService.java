package com.sun.enterprise.tools.classmodel.test;

import org.jvnet.hk2.annotations.Service;

@Service(name="aservice", metadata="a=1,b=2")
public class AService implements AContract {

}
