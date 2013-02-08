/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.simple;

import javax.batch.annotation.ItemProcessor;
import javax.batch.annotation.ProcessItem;

@ItemProcessor("SimpleItemProcessor")
@javax.inject.Named("SimpleItemProcessor")
public class SimpleItemProcessor
    implements javax.batch.api.ItemProcessor<String, String> {

    @ProcessItem
    @Override
    public String processItem(String t) throws Exception {
        String[] record = t.split(", ");
        
    //EMP-ID, MONTH-YEAR, SALARY, TAX%, MEDICARE%, OTHER
        int salary = Integer.valueOf(record[2]);
        double tax = Double.valueOf(record[3]);
        double mediCare = Double.valueOf(record[4]);
        StringBuilder sb = new StringBuilder(t);
        sb.append(", ").append(salary * tax / 100);
        sb.append(", ").append(salary * mediCare / 100);
        sb.append(", ").append(salary - (salary * tax / 100) - (salary * mediCare / 100));
        return sb.toString();
    }
    
}
