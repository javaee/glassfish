Drop table O_CUSTOMER;

CREATE TABLE O_CUSTOMER (
    c_id            integer not null,
    c_phone         char(16)
);

Drop table DUMMY;

CREATE TABLE DUMMY (
    c_id            integer not null,
    c_phone         char(16)
);

INSERT INTO DUMMY VALUES(1, 'wxyz');
