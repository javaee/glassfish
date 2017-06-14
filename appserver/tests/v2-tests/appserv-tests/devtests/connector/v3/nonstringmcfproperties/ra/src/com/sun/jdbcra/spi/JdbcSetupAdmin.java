package com.sun.jdbcra.spi;

public interface JdbcSetupAdmin {

    public void setTableName(String db); 

    public String getTableName();

    public void setJndiName(String name); 

    public String getJndiName(); 

    public void setSchemaName(String name);

    public String getSchemaName();

    public void setNoOfRows(Integer i);

    public Integer getNoOfRows();

    public boolean checkSetup(); 

}
