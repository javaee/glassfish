package com.acme;

import javax.ejb.*;
import java.util.concurrent.*;

@Remote
public interface RemoteAsync {

    void startTest();

    

}