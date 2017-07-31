
Drop table customer_stmt_wrapper;

CREATE TABLE customer_stmt_wrapper (
    c_id            integer not null,
    c_phone         char(16)
);

INSERT INTO customer_stmt_wrapper VALUES(1, 'wxyz');
INSERT INTO customer_stmt_wrapper VALUES(2, 'pqrs');

Drop table sql_trace;

CREATE TABLE sql_trace (
    classname        char(100),
    methodname	     char(100),
    args	     char(200)
);

Drop table null_entry_table;

CREATE TABLE null_entry_table (
    field1        char(100)
);


Drop table expected_sql_trace;

CREATE TABLE expected_sql_trace (
    classname        char(100),
    methodname       char(100),
    args	     char(200)
);


INSERT INTO expected_sql_trace VALUES('org.apache.derby.client.net.NetConnection40', 'prepareStatement', 'select * from customer_stmt_wrapper;1003;1007;');

INSERT INTO expected_sql_trace VALUES('com.sun.gjc.spi.jdbc40.PreparedStatementWrapper40', 'getConnection', '');

INSERT INTO expected_sql_trace VALUES('org.apache.derby.client.net.NetConnection40', 'prepareStatement', 'INSERT INTO null_entry_table VALUES(?);1003;1007;');

INSERT INTO expected_sql_trace VALUES('com.sun.gjc.spi.jdbc40.PreparedStatementWrapper40', 'setString', '1;');

INSERT INTO expected_sql_trace VALUES('com.sun.gjc.spi.jdbc40.PreparedStatementWrapper40', 'executeUpdate', '');

INSERT INTO expected_sql_trace VALUES('com.sun.gjc.spi.jdbc40.PreparedStatementWrapper40', 'close', '');
