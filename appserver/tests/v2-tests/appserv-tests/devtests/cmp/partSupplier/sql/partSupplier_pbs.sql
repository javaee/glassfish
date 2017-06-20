drop table suppliers;
drop table parts;

create table parts (
    PARTID NUMERIC(12),
    NAME VARCHAR(15),
    COLOR VARCHAR(20),
    WEIGHT NUMERIC(4), 
    PRICE FLOAT(23),
    PRIMARY KEY(PARTID)
);

create table suppliers (
    PARTID NUMERIC(12) not null references parts(PARTID),
    SUPPLIERID NUMERIC(12),
    NAME VARCHAR(15), 
    STATUS NUMERIC(2),
    CITY VARCHAR(50),
    constraint pk_suppliers primary key(SUPPLIERID, PARTID)
);
