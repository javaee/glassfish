package endpoint.ejb;

import javax.jws.WebService;
import javax.xml.ws.WebServiceRef;
import javax.ejb.Stateless;
import javax.ejb.SessionContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.annotation.Resource;
import java.sql.*;
import javax.sql.DataSource;

@WebService(endpointInterface="endpoint.ejb.Hello", targetNamespace="http://endpoint/ejb")
@Stateless
public class HelloEJB implements Hello {

    @Resource private SessionContext ctx;
    @Resource(mappedName="jdbc/__default") private DataSource ds;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String sayHello(String who) {
	System.out.println("**** EJB Called");
	Connection con=null;
	String tableName = "CUSTOMER_rb1";
	String nameEntry = "Vikas";
	String emailEntry= "vikas@sun.com";

	try {
	con = ds.getConnection();
	System.out.println("**** auto commit = " + con.getAutoCommit());

	updateTable(con, tableName, nameEntry, emailEntry);
	readData(con, tableName);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        } finally {
	    try {
	    if(con != null) con.close();
	    } catch (SQLException se) {}
	}
	ctx.setRollbackOnly();
        return "WebSvcTest-Hello " + who;
    }

    private void updateTable(Connection con, String tableName, String name, String email) throws Exception {
        PreparedStatement pStmt = 
             con.prepareStatement("INSERT INTO "+ tableName +" (NAME, EMAIL) VALUES(?,?)");
	pStmt.setString(1, name);
	pStmt.setString(2, email);
        pStmt.executeUpdate();
    }

    private void readData(Connection con, String tableName) throws Exception {
        PreparedStatement pStmt = 
             con.prepareStatement("SELECT NAME, EMAIL FROM "+tableName);
        ResultSet rs = pStmt.executeQuery();
        while(rs.next()){
            System.out.println("NAME="+rs.getString(1)+", EMAIL="+rs.getString(2));
        }
    }
}
