package com.acme;

import java.util.concurrent.*;
import javax.ejb.*;

@Asynchronous
public interface SuperAsync { 
    public Future<String> hello(String name);
}