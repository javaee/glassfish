package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.config.JDBCConnectionPoolConfig;
import com.sun.appserv.management.config.JDBCConnectionPoolConfigKeys;


/**
 */
public class JDBCConnectionPoolTest extends BaseTest
{
    private final Cmd target;

    static final String kName                       = "myJDBCConnectionPool";
    static final String kDatasourceClassname        = "a.b.c";
    static final String kResType                    = "javax.sql.ConnectionPoolDataSource";
    static final String kTransactionIsolationLevel  = "repeatable-read";
    static final String kValidationTableName        = "tab1";

    public JDBCConnectionPoolTest(final String user, 
        final String password, final String host, final int port, 
        final String name, final String datasourceClassname) 
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateJDBCConnectionPoolCmd createCmd = 
                cmdFactory.createCreateJDBCConnectionPoolCmd(name,
                        datasourceClassname, getOptional());

        final DeleteJDBCConnectionPoolCmd deleteCmd = 
                cmdFactory.createDeleteJDBCConnectionPoolCmd(name);

        final PipeCmd p1 = new PipeCmd(connectCmd, createCmd);
        final PipeCmd p2 = new PipeCmd(p1, new VerifyCreateCmd());
        final PipeCmd p3 = new PipeCmd(connectCmd, deleteCmd);

        final CmdChainCmd chainCmd = new CmdChainCmd();
        chainCmd.addCmd(p2);
        chainCmd.addCmd(p3);

        target = chainCmd;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }


    public static void main(String[] args) throws Exception
    {
        new JDBCConnectionPoolTest(
                "admin", "password", "localhost", 8686, kName, 
                kDatasourceClassname).run();
    }

    private Map getOptional()
    {
        final Map optional = new HashMap();
        optional.put(JDBCConnectionPoolConfigKeys.TRANSACTION_ISOLATION_LEVEL_KEY,
                kTransactionIsolationLevel);
        optional.put(JDBCConnectionPoolConfigKeys.VALIDATION_TABLE_NAME_KEY,
                kValidationTableName);
        optional.put(JDBCConnectionPoolConfigKeys.RES_TYPE_KEY, kResType);
        return optional;
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private JDBCConnectionPoolConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (JDBCConnectionPoolConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println("Name="+res.getName());
            System.out.println("DatasourceClassname="+ 
                    res.getDatasourceClassname());
            System.out.println("ResType="+ res.getResType());
            System.out.println("SteadyPoolSize="+res.getSteadyPoolSize());
            System.out.println("MaxPoolSize="+res.getMaxPoolSize());
            System.out.println("MaxWaitTimeInMillis="+
                    res.getMaxWaitTimeInMillis());
            System.out.println("PoolResizeQuantity="+
                    res.getPoolResizeQuantity());
            System.out.println("IdleTimeoutInSeconds="+
                    res.getIdleTimeoutInSeconds());
            System.out.println("FailAllConnections="+
                    res.getFailAllConnections());
            System.out.println("TransactionIsolationLevel="+
                    res.getTransactionIsolationLevel());
            System.out.println("IsIsolationLevelGuaranteed="+
                    res.getIsIsolationLevelGuaranteed());
            System.out.println("IsConnectionValidationRequired="+
                    res.getIsConnectionValidationRequired());
            System.out.println("ConnectionValidationMethod="+
                    res.getConnectionValidationMethod());
            System.out.println("ValidationTableName="+
                    res.getValidationTableName());

            return new Integer(0);
        }

    }
}
