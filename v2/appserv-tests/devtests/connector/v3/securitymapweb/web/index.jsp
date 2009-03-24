<%@ page import="javax.naming.*,java.sql.*,javax.sql.*" %>

<%
        Connection conn = null;
        String userName = request.getUserPrincipal().getName();
        try {
            InitialContext initialContext = new InitialContext();
            DataSource ds =
                (DataSource) initialContext.lookup("java:comp/env/jdbc/DS");

            conn = (Connection) ds.getConnection();

            System.out.println(" got the connection : " + conn);

            System.out.println("** insert " + userName + " into securitymapwebdb");

            PreparedStatement prepStmt =
                conn.prepareStatement("insert into securitymapwebdb values(?)");

            prepStmt.setString(1, userName);

            prepStmt.executeUpdate();


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        out.println("done - " + userName);
%>
