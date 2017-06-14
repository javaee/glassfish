/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.twosteps;

import java.io.Externalizable;

@javax.inject.Named("SimpleItemReader")
public class SimpleItemReader
    extends javax.batch.api.AbstractItemReader<String> {

    private int index = 0;
    
    //EMP-ID, MONTH-YEAR, SALARY, TAX%, MEDICARE%, OTHER
    private String[] items = new String[] {
        "120-01, JAN-2013, 8000, 27, 3, 0",
        "120-02, JAN-2013, 8500, 27, 3, 0",
        "120-03, JAN-2013, 9000, 33, 4, 0",
        "120-04, JAN-2013, 8500, 33, 4, 0",
        "120-05, JAN-2013, 10000, 33, 4, 0",
        "120-06, JAN-2013, 10500, 33, 4, 0",
        "120-07, JAN-2013, 11000, 36, 5, 0",
        "120-08, JAN-2013, 11500, 36, 5, 0",
    };
    
    @Override
    public void open(Externalizable e) throws Exception {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public String readItem() throws Exception {
        return index < items.length ? items[index++] : null;
    }

    @Override
    public Externalizable checkpointInfo() throws Exception {
        return null;
    }
    
}
