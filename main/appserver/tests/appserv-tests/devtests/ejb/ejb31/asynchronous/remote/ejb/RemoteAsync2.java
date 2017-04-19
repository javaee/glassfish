package com.acme;

import javax.ejb.*;
import java.util.concurrent.*;

@Remote
public interface RemoteAsync2 {

    Future<String> helloAsync();

    Future<String> removeAfterCalling();

}