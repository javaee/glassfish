/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.simple;

import java.io.Externalizable;
import java.util.List;
import javax.batch.annotation.CheckpointInfo;
import javax.batch.annotation.Close;
import javax.batch.annotation.ItemWriter;
import javax.batch.annotation.Open;
import javax.batch.annotation.WriteItems;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.context.BatchContext;
import javax.inject.Inject;

@ItemWriter("SimpleItemWriter")
@javax.inject.Named("SimpleItemWriter")
public class SimpleItemWriter
    implements javax.batch.api.ItemWriter<String> {
    
    @Open
    @Override
    public void open(Externalizable e) throws Exception {
    }

    @Close
    @Override
    public void close() throws Exception {
    }

    @WriteItems
    @Override
    public void writeItems(List<String> list) throws Exception {
        StringBuilder sb = new StringBuilder("SimpleItemWriter:");
        for (String s : list) {
            sb.append(" ").append(s);
        }
        System.out.println(sb.toString());
    }

    @CheckpointInfo
    @Override
    public Externalizable checkpointInfo() throws Exception {
        return null;
    }
    
}
