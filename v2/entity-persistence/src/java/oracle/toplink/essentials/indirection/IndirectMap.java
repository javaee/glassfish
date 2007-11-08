/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package oracle.toplink.essentials.indirection;

import java.util.*;

/**
 * IndirectMap allows a domain class to take advantage of TopLink indirection
 * without having to declare its instance variable as a ValueHolderInterface.
 * <p>To use an IndirectMap:<ul>
 * <li> Declare the appropriate instance variable with type Hashtable (jdk1.1)
 * or Map (jdk1.2).
 * <li> Send the message #useTransparentMap(String) to the appropriate
 * CollectionMapping.
 * </ul>
 * TopLink will place an
 * IndirectMap in the instance variable when the containing domain object is read from
 * the datatabase. With the first message sent to the IndirectMap, the contents
 * are fetched from the database and normal Hashtable/Map behavior is resumed.
 *
 * @see oracle.toplink.essentials.mappings.CollectionMapping
 * @see oracle.toplink.essentials.indirection.IndirectList
 * @author Big Country
 * @since TOPLink/Java 2.5
 */
public class IndirectMap extends Hashtable implements IndirectContainer {

    /** Reduce type casting */
    protected Hashtable delegate;

    /** Delegate indirection behavior to a value holder */
    protected ValueHolderInterface valueHolder;
    
    /** The mapping attribute name, used to raise change events. */
    private transient String attributeName;

    /** Store initial size for lazy init. */
    protected int initialCapacity = 11;

    /** Store load factor for lazy init. */
    protected float loadFactor = 0.75f;

    /**
     * PUBLIC:
     * Construct a new, empty IndirectMap with a default
     * capacity and load factor.
     */
    public IndirectMap() {
        this(11);
    }

    /**
     * PUBLIC:
     * Construct a new, empty IndirectMap with the specified initial capacity
     * and default load factor.
     *
     * @param   initialCapacity   the initial capacity of the hashtable
     */
    public IndirectMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * PUBLIC:
     * Construct a new, empty IndirectMap with the specified initial
     * capacity and load factor.
     *
     * @param      initialCapacity   the initial capacity of the hashtable
     * @param      loadFactor        a number between 0.0 and 1.0
     * @exception  IllegalArgumentException  if the initial capacity is less
     *               than or equal to zero, or if the load factor is less than
     *               or equal to zero
     */
    public IndirectMap(int initialCapacity, float loadFactor) {
        super(0);
        this.initialize(initialCapacity, loadFactor);
    }

    /**
     * PUBLIC:
     * Construct a new IndirectMap with the same mappings as the given Map.
     * The IndirectMap is created with a capacity of twice the number of entries
     * in the given Map or 11 (whichever is greater), and a default load factor, which is 0.75.
     * @param m a map containing the mappings to use
     */
    public IndirectMap(Map m) {
        super(0);
        this.initialize(m);
    }

    /**
     * Return the freshly-built delegate.
     */
    protected Hashtable buildDelegate() {
        return (Hashtable)getValueHolder().getValue();
    }

    /**
     * @see java.util.Hashtable#clear()
     */
    public synchronized void clear() {
        this.getDelegate().clear();
    }

    /**
     * @see java.util.Hashtable#clone()
     * This will result in a database query if necessary.
     */

    /*
        There are 3 situations when clone() is called:
        1.    The developer actually wants to clone the collection (typically to modify one
            of the 2 resulting collections). In which case the contents must be read from
            the database.
        2.    A UnitOfWork needs a clone (or backup clone) of the collection. But the
            UnitOfWork checks "instantiation" before cloning collections ("un-instantiated"
            collections are not cloned).
        3.    A MergeManager needs an extra copy of the collection (because the "backup"
            and "target" are the same object?). But the MergeManager checks "instantiation"
            before merging collections (again, "un-instantiated" collections are not merged).
    */
    public synchronized Object clone() {
        IndirectMap result = (IndirectMap)super.clone();
        result.delegate = (Hashtable)this.getDelegate().clone();
        return result;
    }

    /**
     * @see java.util.Hashtable#contains(java.lang.Object)
     */
    public synchronized boolean contains(Object value) {
        return this.getDelegate().contains(value);
    }

    /**
     * @see java.util.Hashtable#containsKey(java.lang.Object)
     */
    public synchronized boolean containsKey(Object key) {
        return this.getDelegate().containsKey(key);
    }

    /**
     * @see java.util.Hashtable#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return this.getDelegate().containsValue(value);
    }

    /**
     * @see java.util.Hashtable#elements()
     */
    public synchronized Enumeration elements() {
        return this.getDelegate().elements();
    }

    /**
     * @see java.util.Hashtable#entrySet()
     */
    public Set entrySet() {
        return new Set (){
            Set delegateSet = IndirectMap.this.getDelegate().entrySet();
            
            public int size(){
                return this.delegateSet.size();
            }
        
            public boolean isEmpty(){
                return this.delegateSet.isEmpty();
            }
        
            public boolean contains(Object o){
                return this.delegateSet.contains(o);
            }
        
            public Iterator iterator(){
                return new Iterator() {
                    Iterator delegateIterator = delegateSet.iterator();
                    Object currentObject;
                    
                    public boolean hasNext() {
                        return this.delegateIterator.hasNext();
                    }
                    
                    public Object next() {
                        this.currentObject = this.delegateIterator.next();
                        return this.currentObject;
                    }
                    
                    public void remove() {
                        raiseRemoveChangeEvent(((Map.Entry)currentObject).getKey(), ((Map.Entry)currentObject).getValue());
                        this.delegateIterator.remove();
                    }
                };
            }
        
            public Object[] toArray(){
                return this.delegateSet.toArray();
            }
    
            public Object[] toArray(Object a[]){
                return this.delegateSet.toArray(a);
            }
    
            public boolean add(Object o){
                return this.delegateSet.add(o);
            }
        
            public boolean remove(Object o){
                if (!(o instanceof Map.Entry)) {
                    return false;
                }
                return (IndirectMap.this.remove(((Map.Entry)o).getKey()) != null);
            }
        
            public boolean containsAll(Collection c){
                return this.delegateSet.containsAll(c);
            }
        
            public boolean addAll(Collection c){
                return this.delegateSet.addAll(c);
            }
        
            public boolean retainAll(Collection c){
                boolean result = false;
                Iterator objects = delegateSet.iterator();
                while (objects.hasNext()) {
                    Map.Entry object = (Map.Entry)objects.next();
                    if (!c.contains(object)) {
                        objects.remove();
                        raiseRemoveChangeEvent(object.getKey(), object.getValue());
                        result = true;
                    }
                }
                return result;
            }
            
            public boolean removeAll(Collection c){
                boolean result = false;
                for (Iterator cs = c.iterator(); cs.hasNext(); ){
                    Object object = cs.next();
                    if ( ! (object instanceof Map.Entry)){
                        continue;
                    }
                    Object removed = IndirectMap.this.remove(((Map.Entry)object).getKey());
                    if (removed != null){
                        result = true;
                    }
                }
                return result;
            }
        
            public void clear(){
                IndirectMap.this.clear();
            }
        
            public boolean equals(Object o){
                return this.delegateSet.equals(o);
            }
            
            public int hashCode(){
                return this.delegateSet.hashCode();
            }
        };
    }

    /**
     * @see java.util.Hashtable#equals(java.lang.Object)
     */
    public synchronized boolean equals(Object o) {
        return this.getDelegate().equals(o);
    }

    /**
     * @see java.util.Hashtable#get(java.lang.Object)
     */
    public synchronized Object get(Object key) {
        return this.getDelegate().get(key);
    }

    /**
     * Check whether the contents have been read from the database.
     * If they have not, read them and set the delegate.
     */
    protected synchronized Hashtable getDelegate() {
        if (delegate == null) {
            delegate = this.buildDelegate();
        }
        return delegate;
    }

    /**
     * Return the mapping attribute name, used to raise change events.
     */
     public String getTopLinkAttributeName() {
         return attributeName;
     }
    
    /**
     * PUBLIC:
     * Return the valueHolder.
     */
    public synchronized ValueHolderInterface getValueHolder() {
        // PERF: lazy initialize value holder and vector as are normally set after creation.
        if (valueHolder == null) {
            valueHolder = new ValueHolder(new Hashtable(initialCapacity, loadFactor));
        }
        return valueHolder;
    }

    /**
     * @see java.util.Hashtable#hashCode()
     */
    public synchronized int hashCode() {
        return this.getDelegate().hashCode();
    }
     
    /**
     * Initialize the instance.
     */
    protected void initialize(int initialCapacity, float loadFactor) {
        this.delegate = null;
        this.loadFactor = loadFactor;
        this.initialCapacity = initialCapacity;
        this.valueHolder = null;
    }

    /**
     * Initialize the instance.
     */
    protected void initialize(Map m) {
        this.delegate = null;
        Hashtable temp = new Hashtable(m);

        this.valueHolder = new ValueHolder(temp);
    }

    /**
     * @see java.util.Hashtable#isEmpty()
     */
    public boolean isEmpty() {
        return this.getDelegate().isEmpty();
    }

    /**
     * PUBLIC:
     * Return whether the contents have been read from the database.
     */
    public boolean isInstantiated() {
        return this.getValueHolder().isInstantiated();
    }

    /**
     * @see java.util.Hashtable#keys()
     */
    public synchronized Enumeration keys() {
        return this.getDelegate().keys();
    }

    /**
     * @see java.util.Hashtable#keySet()
     */
    public Set keySet() {
        
        return new Set (){
            Set delegateSet = IndirectMap.this.getDelegate().keySet();
            
            public int size(){
                return this.delegateSet.size();
            }
        
            public boolean isEmpty(){
                return this.delegateSet.isEmpty();
            }
        
            public boolean contains(Object o){
                return this.delegateSet.contains(o);
            }
        
            public Iterator iterator(){
                return new Iterator() {
                    Iterator delegateIterator = delegateSet.iterator();
                    Object currentObject;
                    
                    public boolean hasNext() {
                        return this.delegateIterator.hasNext();
                    }
                    
                    public Object next() {
                        this.currentObject = this.delegateIterator.next();
                        return this.currentObject;
                    }
                    
                    public void remove() {
                        IndirectMap.this.raiseRemoveChangeEvent(currentObject, IndirectMap.this.getDelegate().get(currentObject));
                        this.delegateIterator.remove();
                    }
                };
            }
        
            public Object[] toArray(){
                return this.delegateSet.toArray();
            }
    
            public Object[] toArray(Object a[]){
                return this.delegateSet.toArray(a);
            }
    
            public boolean add(Object o){
                return this.delegateSet.add(o);
            }
        
            public boolean remove(Object o){
                return (IndirectMap.this.remove(o) != null);
            }
        
            public boolean containsAll(Collection c){
                return this.delegateSet.containsAll(c);
            }
        
            public boolean addAll(Collection c){
                return this.delegateSet.addAll(c);
            }
        
            public boolean retainAll(Collection c){
                boolean result = false;
                Iterator objects = delegateSet.iterator();
                while (objects.hasNext()) {
                    Object object = objects.next();
                    if (!c.contains(object)) {
                        objects.remove();
                        IndirectMap.this.raiseRemoveChangeEvent(object, IndirectMap.this.getDelegate().get(object));
                        result = true;
                    }
                }
                return result;
            }
            
            public boolean removeAll(Collection c){
                boolean result = false;
                for (Iterator cs = c.iterator(); cs.hasNext(); ){
                    if (IndirectMap.this.remove(cs.next()) != null ) {
                        result = true;
                    }
                }
                return result;
            }
        
            public void clear(){
                IndirectMap.this.clear();
            }
        
            public boolean equals(Object o){
                return this.delegateSet.equals(o);
            }
            
            public int hashCode(){
                return this.delegateSet.hashCode();
            }
        };
            
            
    }

    /**
     * @see java.util.Hashtable#put(java.lang.Object, java.lang.Object)
     */
    public synchronized Object put(Object key, Object value) {
        Object oldValue = this.getDelegate().put(key, value);
        if (oldValue != null){
            raiseRemoveChangeEvent(key, oldValue);
        }
        raiseAddChangeEvent(key, value);
        return oldValue;
    }
    

    /**
     * @see java.util.Hashtable#putAll(java.util.Map)
     */
    public synchronized void putAll(Map t) {
        this.getDelegate().putAll(t);
    }

    /**
     * @see java.util.Hashtable#rehash()
     */
    protected void rehash() {
        throw new InternalError("unsupported");
    }

    /**
     * Raise the add change event and relationship maintainence.
     */
    protected void raiseAddChangeEvent(Object key, Object value) {
        // this is where relationship maintenance would go
    }

    /**
     * Raise the remove change event.
     */
    protected void raiseRemoveChangeEvent(Object key, Object value) {
        // this is where relationship maintenance would go
    }

    /**
     * @see java.util.Hashtable#remove(java.lang.Object)
     */
    public synchronized Object remove(Object key) {
        Object value = this.getDelegate().remove(key);
        if (value != null){
            raiseRemoveChangeEvent(key, value);
        }
        return value;
    }

    /**
     * Set the mapping attribute name, used to raise change events.
     * This is required if the change listener is set.
     */
     public void setTopLinkAttributeName(String attributeName) {
         this.attributeName = attributeName;
     }
     
    /**
     * PUBLIC:
     * Set the value holder.
     */
    public void setValueHolder(ValueHolderInterface valueHolder) {
        this.delegate = null;
        this.valueHolder = valueHolder;
    }

    /**
     * @see java.util.Hashtable#size()
     */
    public int size() {
        return this.getDelegate().size();
    }

    /**
     * PUBLIC:
     * Use the Hashtable.toString(); but wrap it with braces to indicate
     * there is a bit of indirection.
     * Don't allow this method to trigger a database read.
     * @see java.util.Hashtable#toString()
     */
    public String toString() {
        if (ValueHolderInterface.shouldToStringInstantiate) {
            return this.getDelegate().toString();
        }
        if (this.isInstantiated()) {
            return "{" + this.getDelegate().toString() + "}";
        } else {
            return "{" + oracle.toplink.essentials.internal.helper.Helper.getShortClassName(this.getClass()) + ": not instantiated}";
        }
    }

    /**
     * @see java.util.Hashtable#values()
     */
    public Collection values() {
        return new Collection() {
            protected Collection delegateCollection = IndirectMap.this.getDelegate().values();

            public int size(){
                return delegateCollection.size();
            }
            
            public boolean isEmpty(){
                return delegateCollection.isEmpty();
            }
            
            public boolean contains(Object o){
                return delegateCollection.contains(o);
            }
            
            public Iterator iterator() {
                return new Iterator() {
                    Iterator delegateIterator = delegateCollection.iterator();
                    Object currentObject;
                    
                    public boolean hasNext() {
                        return this.delegateIterator.hasNext();
                    }
                    
                    public Object next() {
                        this.currentObject = this.delegateIterator.next();
                        return this.currentObject;
                    }
                    
                    public void remove() {
                        Iterator iterator = IndirectMap.this.getDelegate().entrySet().iterator();
                        while (iterator.hasNext()){
                            Map.Entry entry = (Map.Entry)iterator.next();
                            if (entry.getValue().equals(currentObject)){
                                IndirectMap.this.raiseRemoveChangeEvent(entry.getKey(), entry.getValue());
                            }
                            
                        }
                        this.delegateIterator.remove();
                    }
                };
            }
        
            public Object[] toArray(){
                return this.delegateCollection.toArray();
            }
            
            public Object[] toArray(Object a[]){
                return this.delegateCollection.toArray(a);
            }
            
            public boolean add(Object o){
                return this.delegateCollection.add(o);
            }
            
            public boolean remove(Object o){
                Iterator iterator = IndirectMap.this.getDelegate().entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry entry = (Map.Entry)iterator.next();
                    if (entry.getValue().equals(o)){
                        IndirectMap.this.raiseRemoveChangeEvent(entry.getKey(), entry.getValue());
                    }
                    return true;    
                }
                return false;
            }
            
            public boolean containsAll(Collection c){
                return this.delegateCollection.containsAll(c);
            }
            
            public boolean addAll(Collection c){
                return this.delegateCollection.addAll(c);
            }
            
            public boolean removeAll(Collection c){
                boolean result = false;
                for (Iterator iterator = c.iterator(); iterator.hasNext();){
                    if (remove(iterator.next()) ){
                        result = true;
                    }
                }
                return result;
            }
            
            public boolean retainAll(Collection c){
                boolean result = false;
                for (Iterator iterator = IndirectMap.this.entrySet().iterator(); iterator.hasNext();){
                    Map.Entry entry = (Map.Entry)iterator.next();
                    if (! c.contains(entry.getValue()) ) {
                        iterator.remove();
                        result = true;
                    }
                }
                return result;
            }
            
            public void clear(){
                IndirectMap.this.clear();
            }
            
            
            public boolean equals(Object o){
                return this.delegateCollection.equals(o);
            }
            
            public int hashCode(){
                return this.delegateCollection.hashCode();
            }
            
        };
    }
}
