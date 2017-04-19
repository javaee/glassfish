package com.acme;

import java.util.concurrent.*;
import javax.ejb.*;

public interface SuperAsync { 
    public Future<String> hello(String name);
}