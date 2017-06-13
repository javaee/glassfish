drop table suppliers;
drop table parts;

create table parts (
    PARTID INT PRIMARY KEY NOT NULL,
    NAME VARCHAR(15),
    COLOR VARCHAR(20),
    WEIGHT INT,
    PRICE FLOAT
);

create table suppliers (
    PARTID INT NOT NULL references parts(PARTID),
    SUPPLIERID INT NOT NULL,
    NAME VARCHAR(15), 
    STATUS INT,
    CITY VARCHAR(50),
    constraint pk_suppliers primary key (SUPPLIERID, PARTID)
);

commit;

quit;
