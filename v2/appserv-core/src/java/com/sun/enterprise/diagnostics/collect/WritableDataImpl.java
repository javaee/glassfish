/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.diagnostics.collect;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.enterprise.diagnostics.Data;

/**
 * 
 * @author Manisha Umbarje
 */
public class WritableDataImpl implements WritableData {
    
    protected List<Data> children;
    
    protected List<String> values;
    
    protected List<List<String>> table;
    
    protected String source;
    protected String type;
    
    private static int INITIAL_CAPACITY = 5;
    
    public WritableDataImpl() {
        this("", DataType.CONTAINER);
    }
    public WritableDataImpl(String type) {
        this("",type);
    }
    /** Creates a new instance of WritableDataImpl */
    public WritableDataImpl(String source, String type) {
        this(source,type, 5,20);
    }
    
    public WritableDataImpl(String source, String type,
            int initialValuesCapacity) {
        this(source, type, INITIAL_CAPACITY, initialValuesCapacity);
        
    }
    public WritableDataImpl(String source, String type,
        int initialChildrenCapacity, int initialValuesCapacity) {
        this(source,type, initialChildrenCapacity,
                initialValuesCapacity,INITIAL_CAPACITY);
        
    }
    public WritableDataImpl(String source, String type, 
            int initialChildrenCapacity, 
        int initialValuesCapacity, int initialTableCapacity) {
        
        this.source= source;
        this.type = type;
        children = new ArrayList(initialChildrenCapacity);
        values = new ArrayList(initialValuesCapacity);
        table = new ArrayList(initialTableCapacity);
        
    }
    
    public void addChild(Data dataObj) {
        if(dataObj != null)
            children.add(dataObj);
    }
    
    public void addValue(String value) {
        values.add(value);
    }
    
    public void addRow(List<String> list) {
        if(list != null)
            table.add(list);
    }
    public Iterator<Data> getChildren() {
        return children.iterator();
    }
    
    
    public Iterator<String> getValues() {
        return values.iterator();
    }
    public Iterator<Iterator<String>> getTable() {
        List<Iterator<String>> listOfIterators = new ArrayList();
        Iterator<List<String>> iterator = table.iterator();
        while(iterator.hasNext()) {
            List row = iterator.next();
            listOfIterators.add(row.iterator());
        }
        return listOfIterators.iterator();
    }
    public String getSource() {
        return source;
    }
    public String getType() {
        return type;
    }
}
