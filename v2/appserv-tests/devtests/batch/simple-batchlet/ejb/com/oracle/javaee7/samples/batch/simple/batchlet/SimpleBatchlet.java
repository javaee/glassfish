/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.simple.batchlet;

import javax.inject.Inject;


@javax.inject.Named
public class SimpleBatchlet
    implements javax.batch.api.Batchlet {

    @Override
    public String process() throws Exception {
        return  "GREAT_SUCCESS";
    }
    
    @Override
    public void  stop() throws Exception {
    }
    
}
