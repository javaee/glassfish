package com.acme;

import javax.ejb.*;
import java.util.concurrent.*;

@Remote
public interface RemoteAsync {

    Future<String> helloAsync();

    Future<String> removeAfterCalling();

}