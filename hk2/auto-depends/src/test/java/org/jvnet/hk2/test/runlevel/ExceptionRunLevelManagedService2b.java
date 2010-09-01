package org.jvnet.hk2.test.runlevel;

import java.lang.reflect.Constructor;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

/**
 * Used in testing exception handling, also belonging to another RunLevel environment.
 * 
 * @author Jeff Trent
 */
@Service
@RunLevel(value=2, environment=Exception.class)
public class ExceptionRunLevelManagedService2b implements RunLevelContract, PostConstruct, PreDestroy {

  public static Constructor<RuntimeException> exceptionCtor;
  public static int constructCount;
  public static int destroyCount;

  @Override
  public void postConstruct() {
    constructCount++;
    if (null != exceptionCtor) {
      try {
        RuntimeException e = exceptionCtor.newInstance((Object[])null);
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        exceptionCtor = null;
      }
    }
  }

  @Override
  public void preDestroy() {
    destroyCount++;
    if (null != exceptionCtor) {
      try {
        RuntimeException e = exceptionCtor.newInstance((Object[])null);
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        exceptionCtor = null;
      }
    }
  }
  
  
}
