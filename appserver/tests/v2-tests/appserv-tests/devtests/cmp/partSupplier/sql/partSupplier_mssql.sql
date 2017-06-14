drop table suppliers
drop table parts
go

create table parts (
    PARTID INT PRIMARY KEY,
    NAME VARCHAR(15),
    COLOR VARCHAR(20),
    WEIGHT INT,
    PRICE FLOAT
)

create table suppliers (
    PARTID INT not null references parts(PARTID),
    SUPPLIERID INT,
    NAME VARCHAR(15), 
    STATUS INT,
    CITY VARCHAR(50),
    constraint pk_suppliers primary key(SUPPLIERID, PARTID)
)

go
