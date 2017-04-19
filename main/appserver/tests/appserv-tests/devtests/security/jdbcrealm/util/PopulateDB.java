package com.sun.devtests.security.jdbcrealm;

import java.sql.*;
import java.security.*;

import sun.misc.BASE64Encoder;

public class PopulateDB {

    // username/passwords
    private static final String[][] users= { {"xyz", "xyz"}, 
                                             {"abc", "abc"},
                                             {"qwert", "qwert"},
                                             {"testy", "testy"} }; 

    private static final String[][] users_BASE64= 
                                    { {"qwertBASE64", "qwertBASE64"} }; 
    private static final String[][] users_HEX= 
                                    { {"qwertHEX", "qwertHEX"} }; 

    private static final String[][] users_MD2_BASE64= 
                          { {"qwertMD2BASE64", "qwertMD2BASE64"} }; 
    private static final String[][] users_MD5_BASE64= 
                          { {"qwertMD5BASE64", "qwertMD5BASE64"} }; 
    private static final String[][] users_SHA_BASE64= 
                          { {"qwertSHABASE64", "qwertSHABASE64"} }; 
    private static final String[][] users_SHA256_BASE64= 
                          { {"qwertSHA256BASE64", "qwertSHA256BASE64"} }; 
    private static final String[][] users_SHA384_BASE64= 
                          { {"qwertSHA384BASE64", "qwertSHA384BASE64"} }; 
    private static final String[][] users_SHA512_BASE64= 
                          { {"qwertSHA512BASE64", "qwertSHA512BASE64"} }; 

    private static final String[][] users_MD2_HEX= 
                          { {"qwertMD2HEX", "qwertMD2HEX"} }; 
    private static final String[][] users_MD5_HEX= 
                          { {"qwertMD5HEX", "qwertMD5HEX"} }; 
    private static final String[][] users_SHA_HEX= 
                          { {"qwertSHAHEX", "qwertSHAHEX"} }; 
    private static final String[][] users_SHA256_HEX= 
                          { {"qwertSHA256HEX", "qwertSHA256HEX"} }; 
    private static final String[][] users_SHA384_HEX= 
                          { {"qwertSHA384HEX", "qwertSHA384HEX"} }; 
    private static final String[][] users_SHA512_HEX= 
                          { {"qwertSHA512HEX", "qwertSHA512HEX"} }; 

    // username/groupname
    private static final String[][] groups= { {"xyz", "staff"},
                                              {"xyz", "employee"},
                                              {"abc", "staff"},
                                              {"abc", "employee"},
                                              {"qwert", "staff"},
                                              {"qwertBASE64", "staff"},
                                              {"qwertHEX", "staff"},

//                                               {"qwertMD2BASE64", "staff"},
//                                               {"qwertMD5BASE64", "staff"},
//                                               {"qwertSHABASE64", "staff"},
//                                               {"qwertSHA256BASE64", "staff"},
//                                               {"qwertSHA384BASE64", "staff"},
//                                               {"qwertSHA512BASE64", "staff"},

//                                               {"qwertMD2HEX", "staff"},
//                                               {"qwertMD5HEX", "staff"},
//                                               {"qwertSHAHEX", "staff"},
//                                               {"qwertSHA256HEX", "staff"},
//                                               {"qwertSHA384HEX", "staff"},
//                                               {"qwertSHA512HEX", "staff"},

                                              {"testy", "staff"} }; 

    private static final String URL_OPTION       = "-url";
    private static final String VERBOSE_OPTION   = "-verbose";
    private static final String DB_DRIVER_OPTION = "-dbDriver";
    private static final String USERNAME_OPTION = "-username";
    private static final String PASSWORD_OPTION = "-password";

    private static boolean verbose = false;

    private static String INSERT_USER_STMT_CLEAR = 
        "insert into USER_TABLE values (?, ?)";
    private static String INSERT_USER_STMT_BASE64 = 
        "insert into USER_TABLE_BASE64 values (?, ?)";
    private static String INSERT_USER_STMT_HEX = 
        "insert into USER_TABLE_HEX values (?, ?)";

    private static String INSERT_USER_STMT_MD2_BASE64 = 
        "insert into USER_TABLE_MD2_BASE64 values (?, ?)";
    private static String INSERT_USER_STMT_MD5_BASE64 = 
        "insert into USER_TABLE_MD5_BASE64 values (?, ?)";
    private static String INSERT_USER_STMT_SHA_BASE64 = 
        "insert into USER_TABLE_SHA_BASE64 values (?, ?)";
    private static String INSERT_USER_STMT_SHA256_BASE64 = 
        "insert into USER_TABLE_SHA256_BASE64 values (?, ?)";
    private static String INSERT_USER_STMT_SHA384_BASE64 = 
        "insert into USER_TABLE_SHA384_BASE64 values (?, ?)";
    private static String INSERT_USER_STMT_SHA512_BASE64 = 
        "insert into USER_TABLE_SHA512_BASE64 values (?, ?)";

    private static String INSERT_USER_STMT_MD2_HEX = 
        "insert into USER_TABLE_MD2_HEX values (?, ?)";
    private static String INSERT_USER_STMT_MD5_HEX = 
        "insert into USER_TABLE_MD5_HEX values (?, ?)";
    private static String INSERT_USER_STMT_SHA_HEX = 
        "insert into USER_TABLE_SHA_HEX values (?, ?)";
    private static String INSERT_USER_STMT_SHA256_HEX = 
        "insert into USER_TABLE_SHA256_HEX values (?, ?)";
    private static String INSERT_USER_STMT_SHA384_HEX = 
        "insert into USER_TABLE_SHA384_HEX values (?, ?)";
    private static String INSERT_USER_STMT_SHA512_HEX = 
        "insert into USER_TABLE_SHA512_HEX values (?, ?)";

    private static String INSERT_GROUP_STMT = 
        "insert into GROUP_TABLE values (?, ?)";

    private static void verbose(String msg) {
        if( verbose )
            System.out.println(msg);
    }

    private static void usage() {
        System.out.println("usage: java PopulateDB -url <url>");
    }

    public static void main(String[] args) throws Exception {

        String dbURL = null;
        String dbDriverClass = "org.apache.derby.jdbc.ClientDriver";
        String username = null;
        String password = null;

        for(int i=0; i<args.length; i++) {
            verbose("Arg[" + i + "] " +args[i]);
            if( args[i].intern() == URL_OPTION.intern() ) {
                dbURL = args[++i];
            } else if( args[i].intern() == VERBOSE_OPTION.intern() ) {
                verbose = true;
            } else if( args[i].intern() == DB_DRIVER_OPTION.intern() ) {
                dbDriverClass = args[++i];;
            } else if( args[i].intern() == USERNAME_OPTION.intern() ) {
                username = args[++i];
            } else if( args[i].intern() == PASSWORD_OPTION.intern() ) {
                password = args[++i];
            } else {
                usage();
                System.exit(1);
            }
        }

        if( dbURL == null ) {
            usage();
            System.exit(1);
        }

        verbose("db url: " + dbURL);
        verbose("db driver: " + dbDriverClass);
        verbose("db user: " + username);
        verbose("db pass: " + password);

        Class.forName(dbDriverClass);
        Connection con =  null;
        if( username == null )
            con = DriverManager.getConnection(dbURL);
        else
            con = DriverManager.getConnection(dbURL, username, password);

        // populate the clear user table
        PreparedStatement ps = con.prepareStatement(INSERT_USER_STMT_CLEAR);
        for(int i=0; i<users.length; i++) {
            ps.setString(1, users[i][0]);
            ps.setString(2, users[i][1]);
            ps.executeUpdate();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_BASE64);
        BASE64Encoder base64 = new BASE64Encoder();
        for(int i=0; i<users_BASE64.length; i++) {
            ps.setString(1, users_BASE64[i][0]);
            ps.setString(2, new String(base64.encode(users_BASE64[i][1].getBytes())));
            ps.executeUpdate();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_HEX);
        for(int i=0; i<users_HEX.length; i++) {
            ps.setString(1, users_HEX[i][0]);
            ps.setString(2, hexConvert(users_HEX[i][1].getBytes()));
            ps.executeUpdate();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_MD2_BASE64);
        MessageDigest md2 = MessageDigest.getInstance("MD2");
        for(int i=0; i<users_MD2_BASE64.length; i++) {
            ps.setString(1, users_MD2_BASE64[i][0]);
            ps.setString(2, base64.encode(md2.digest(users_MD2_BASE64[i][1].getBytes())));
            ps.executeUpdate();
            md2.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_MD5_BASE64);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for(int i=0; i<users_MD5_BASE64.length; i++) {
            ps.setString(1, users_MD5_BASE64[i][0]);
            ps.setString(2, base64.encode(md5.digest(users_MD5_BASE64[i][1].getBytes())));
            ps.executeUpdate();
            md5.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_SHA_BASE64);
        MessageDigest sha = MessageDigest.getInstance("SHA");
        for(int i=0; i<users_SHA_BASE64.length; i++) {
            ps.setString(1, users_SHA_BASE64[i][0]);
            ps.setString(2, base64.encode(sha.digest(users_SHA_BASE64[i][1].getBytes())));
            ps.executeUpdate();
            sha.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_SHA256_BASE64);
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for(int i=0; i<users_SHA256_BASE64.length; i++) {
            ps.setString(1, users_SHA256_BASE64[i][0]);
            ps.setString(2, base64.encode(sha256.digest(users_SHA256_BASE64[i][1].getBytes())));
            ps.executeUpdate();
            sha256.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_SHA384_BASE64);
        MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
        for(int i=0; i<users_SHA384_BASE64.length; i++) {
            ps.setString(1, users_SHA384_BASE64[i][0]);
            ps.setString(2, base64.encode(sha384.digest(users_SHA384_BASE64[i][1].getBytes())));
            ps.executeUpdate();
            sha384.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_SHA512_BASE64);
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        for(int i=0; i<users_SHA512_BASE64.length; i++) {
            ps.setString(1, users_SHA512_BASE64[i][0]);
            ps.setString(2, base64.encode(sha512.digest(users_SHA512_BASE64[i][1].getBytes())));
            ps.executeUpdate();
            sha512.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_MD2_HEX);
        for(int i=0; i<users_MD2_HEX.length; i++) {
            ps.setString(1, users_MD2_HEX[i][0]);
            ps.setString(2, hexConvert(md2.digest(users_MD2_HEX[i][1].getBytes())));
            ps.executeUpdate();
            md2.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_MD5_HEX);
        for(int i=0; i<users_MD5_HEX.length; i++) {
            ps.setString(1, users_MD5_HEX[i][0]);
            ps.setString(2, hexConvert(md5.digest(users_MD5_HEX[i][1].getBytes())));
            ps.executeUpdate();
            md5.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_SHA_HEX);
        for(int i=0; i<users_SHA_HEX.length; i++) {
            ps.setString(1, users_SHA_HEX[i][0]);
            ps.setString(2, hexConvert(sha.digest(users_SHA_HEX[i][1].getBytes())));
            ps.executeUpdate();
            sha.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_SHA256_HEX);
        for(int i=0; i<users_SHA256_HEX.length; i++) {
            ps.setString(1, users_SHA256_HEX[i][0]);
            ps.setString(2, hexConvert(sha256.digest(users_SHA256_HEX[i][1].getBytes())));
            ps.executeUpdate();
            sha256.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_SHA384_HEX);
        for(int i=0; i<users_SHA384_HEX.length; i++) {
            ps.setString(1, users_SHA384_HEX[i][0]);
            ps.setString(2, hexConvert(sha384.digest(users_SHA384_HEX[i][1].getBytes())));
            ps.executeUpdate();
            sha384.reset();
        }
        ps.close();

        ps = con.prepareStatement(INSERT_USER_STMT_SHA512_HEX);
        for(int i=0; i<users_SHA512_HEX.length; i++) {
            ps.setString(1, users_SHA512_HEX[i][0]);
            ps.setString(2, hexConvert(sha512.digest(users_SHA512_HEX[i][1].getBytes())));
            ps.executeUpdate();
            sha512.reset();
        }
        ps.close();

        // populate the group table
        ps = con.prepareStatement(INSERT_GROUP_STMT);
        for(int i=0; i<groups.length; i++) {
            ps.setString(1, groups[i][0]);
            ps.setString(2, groups[i][1]);
            ps.executeUpdate();
        }
        ps.close();
    }

    private static String hexConvert(byte bytes[]) {

        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(convertDigit((int) (bytes[i] >> 4)));
            sb.append(convertDigit((int) (bytes[i] & 0x0f)));
        }
        return (sb.toString());

    }

    private static char convertDigit(int value) {

        value &= 0x0f;
        if (value >= 10)
            return ((char) (value - 10 + 'a'));
        else
            return ((char) (value + '0'));

    }


}
