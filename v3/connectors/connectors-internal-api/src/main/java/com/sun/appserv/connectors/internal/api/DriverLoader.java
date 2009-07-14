package com.sun.appserv.connectors.internal.api;

import java.util.Set;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface DriverLoader {

    /**
     * Fetch the DataSource/Driver implementation class names for the particular 
     * dbVendor and resource type. Sometimes an already stored datasource
     * classname is used in this method.
     * @param datasourceClassName
     * @param dbVendor
     * @param resType
     * @return set of implementation class names for the dbvendor.
     */    
    public Set<String> getJdbcDriverClassNames(String dbVendor, String resType);    
}