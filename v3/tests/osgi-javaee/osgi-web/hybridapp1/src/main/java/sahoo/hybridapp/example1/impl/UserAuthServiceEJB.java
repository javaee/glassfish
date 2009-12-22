package sahoo.hybridapp.example1.impl;

import sahoo.hybridapp.example1.UserAuthService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

@Stateless
public class UserAuthServiceEJB implements UserAuthService
{

    @Resource(mappedName= Activator.dsName)
    private DataSource ds;

    @PostConstruct
    public void postConstruct() {
        System.out.println("UserAuthServiceEJB.postConstruct");
    }

    public boolean login(String name, String password)
    {
        System.out.println("UserAuthServiceEJBuser: logging in " + name);
        Connection c = null;
        Statement s = null;
        try
        {
            c = ds.getConnection();
            s = c.createStatement();
            String sql = "select count(*) as record_count from " +
                    Activator.tableName +" where name = '" + name +
                    "' and password= '" + password + "'";
            System.out.println("sql = " + sql);
            ResultSet rs = s.executeQuery(sql);
            rs.next();
            if (rs.getInt("record_count") == 1) {
                System.out.println("Login successful");
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (c!= null) c.close();
                if (s!=null) s.close();
            }
            catch (Exception e)
            {
            }
        }
        return false;
    }

    public boolean register(String name, String password)
    {
        System.out.println("UserAuthServiceEJB: registering " + name);
        Connection c = null;
        Statement s = null;
        try
        {
            c = ds.getConnection();
            s = c.createStatement();
            String sql = "insert into " + Activator.tableName +
                    " values('" + name + "', '" + password + "')";
            System.out.println("sql = " + sql);
            s.executeUpdate(sql);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (c!= null) c.close();
                if (s!=null) s.close();
            }
            catch (Exception e)
            {
            }
        }
        return false;
    }
}
