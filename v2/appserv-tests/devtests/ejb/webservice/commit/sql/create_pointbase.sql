Drop table O_Customer;

CREATE TABLE O_Customer (
    c_id            integer NOT NULL PRIMARY KEY,
    c_phone         char(32)
);

INSERT INTO O_Customer values (2, 'foo');