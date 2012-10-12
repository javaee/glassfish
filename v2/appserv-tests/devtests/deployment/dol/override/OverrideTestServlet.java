package override;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;

public class OverrideTestServlet extends HttpServlet
{

    @Resource(name="myDS5and6", mappedName="jdbc/__default")
    private DataSource myDS5;

    private DataSource myDS6;

    @Resource(name="myDS7", lookup="jdbc/noexist", description="original", shareable=true, authenticationType=Resource.AuthenticationType.CONTAINER)
    private DataSource myDS7;

    @Resource(name="myDS8", mappedName="jdbc/noexist2")
    private DataSource myDS8;

    public void
    init () throws ServletException
    {
        super.init();
        System.out.println("init()...");
    }

    public void
    service (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        try {
            InitialContext ic = new InitialContext();

            DataSource myDS3 = (DataSource)ic.lookup("java:comp/env/myDS3");
            int loginTimeout3 = myDS3.getLoginTimeout();
            System.out.println("myDS3 login timeout = " + loginTimeout3);

            DataSource myDS4 = (DataSource)ic.lookup("java:comp/env/myDS4");
            int loginTimeout4 = myDS4.getLoginTimeout();
            System.out.println("myDS4 login timeout = " + loginTimeout4);

            int loginTimeout5 = myDS5.getLoginTimeout();
            System.out.println("myDS5 login timeout = " + loginTimeout5);

            int loginTimeout6 = myDS6.getLoginTimeout();
            System.out.println("myDS6 login timeout = " + loginTimeout6);

            int loginTimeout7 = myDS7.getLoginTimeout();
            System.out.println("myDS7 login timeout = " + loginTimeout7);

            int loginTimeout8 = myDS8.getLoginTimeout();
            System.out.println("myDS8 login timeout = " + loginTimeout8);

        } catch(Exception ex) {
            throw new IllegalStateException("Cannot get login timeout: " + ex);
        } 
    }
}
