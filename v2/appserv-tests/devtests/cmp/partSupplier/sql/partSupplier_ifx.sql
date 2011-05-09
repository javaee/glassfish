drop table SUPPLIERS;
drop table PARTS;

create table PARTS (
    PARTID INT NOT NULL PRIMARY KEY,
    NAME VARCHAR(15),
    COLOR VARCHAR(20),
    WEIGHT INT,
    PRICE FLOAT
);

create table SUPPLIERS (
    PARTID INT NOT NULL references PARTS(PARTID),
    SUPPLIERID INT NOT NULL,
    NAME VARCHAR(15), 
    STATUS INT,
    CITY VARCHAR(50),
    primary key(SUPPLIERID, PARTID)
);

commit;

quit;
