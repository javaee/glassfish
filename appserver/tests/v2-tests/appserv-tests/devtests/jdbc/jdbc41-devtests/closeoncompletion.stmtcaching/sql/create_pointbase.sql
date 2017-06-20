drop table customer_stmt_wrapper;
CREATE TABLE customer_stmt_wrapper (
    c_id            integer not null,
    c_phone         char(16)
);

INSERT INTO customer_stmt_wrapper VALUES(2, 'shal');
