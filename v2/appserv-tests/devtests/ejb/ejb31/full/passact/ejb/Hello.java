package com.acme;

import javax.ejb.Remote;
import java.util.concurrent.Future;
import javax.ejb.Asynchronous;

@Remote
public interface Hello {

    String hello();

    boolean passivatedAndActivated();

}