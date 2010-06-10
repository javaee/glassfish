package org.jvnet.hk2.component;

import java.util.List;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

/**
 * This is the handle returned by the InhabitantHandler
 * when a service is registered.
 * <p>
 * If you need to unregister a service, you will need to
 * use this handle to do so.
 * <p>
 * A test-only, helper construct.
 * 
 * @author Jeff Trent
 */
public interface InhabitantHandle<T> {

  /**
   * Adds an index and name to the given handle.
   * 
   * @param index usually the contract
   * @param name optionally the name
   * @throws ComponentException
   */
  public void addIndex(String index) throws ComponentException;
  
  /**
   * Adds an index and name to the given handle.
   * 
   * @param index usually the contract
   * @param name optionally the name
   * @throws ComponentException
   */
  public void addIndex(String index, String name) throws ComponentException;

  /**
   * The Inhabitant represented by this handle.
   * 
   * @return The inhabitant represented by this handle
   */
  public Inhabitant<T> getInhabitant();
  
  /**
   * Retrieves the list of indices/contracts belonging to this inhabitant.
   * 
   * @return the list of indices/contracts belonging to this inhabitant
   */
  public List<String> getIndices();
  
  /**
   * The current set of properties associated
   * with this handle
   * 
   * @return The current set of properties
   * associated with this handle.
   */
  public MultiMap<String,String> getMetadata();

  /**
   * Checks whether this handle has been committed to the habitat
   * 
   * @return true if this handle has been committed
   */
  public boolean isCommitted();
  
  /**
   * Commits the inhabitant to the habitat (along with all of its related indexes).
   * 
   * Once commit is called, the this handle is no longer modifiable
   */
  public void commit();

  /**
   * Unregisters the inhabitant from the habitat (along with all of its related indexes).
   * 
   * Once release is called, this handle is modifiable
   */
  public void release();
}
