
Drop table customer_stmt_wrapper;

CREATE TABLE customer_stmt_wrapper (
    c_id            integer not null,
    c_phone         char(16)
);

INSERT INTO customer_stmt_wrapper VALUES(1, 'wxyz');
INSERT INTO customer_stmt_wrapper VALUES(2, 'pqrs');

Drop table COMPARE_RECORDS;

CREATE TABLE COMPARE_RECORDS(
    REC_STMT_TYPE	char(5),
    REC_STMT		integer
);
