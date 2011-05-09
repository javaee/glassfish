drop table suppliers;
drop table parts;

create table parts (
    PARTID INT PRIMARY KEY,
    NAME VARCHAR2(15),
    COLOR VARCHAR2(20),
    WEIGHT INT,
    PRICE FLOAT
);

create table suppliers (
    PARTID INT not null references parts(PARTID),
    SUPPLIERID INT,
    NAME VARCHAR2(15), 
    STATUS INT,
    CITY VARCHAR2(50),
    constraint pk_suppliers primary key(SUPPLIERID, PARTID)
);

commit;

quit;
