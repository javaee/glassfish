package test;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.*;

@Singleton
@Startup
@Remote(Hello.class)
public class HelloBean implements Hello {
    @Asynchronous
    public Future<String> getThreadNameId() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        Thread th = Thread.currentThread();
	return new AsyncResult<String>(th.getName() + " " + System.identityHashCode(th));
    }
}
