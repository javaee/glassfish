/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.simple;

import java.io.Externalizable;
import java.util.List;
@javax.inject.Named("SimpleItemWriter")
public class SimpleItemWriter
    extends javax.batch.api.AbstractItemWriter<String> {
    
    @Override
    public void open(Externalizable e) throws Exception {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void writeItems(List<String> list) throws Exception {
        StringBuilder sb = new StringBuilder("SimpleItemWriter:");
        for (String s : list) {
            sb.append(" ").append(s);
        }
        System.out.println(sb.toString());
    }

    @Override
    public Externalizable checkpointInfo() throws Exception {
        return null;
    }
    
}
