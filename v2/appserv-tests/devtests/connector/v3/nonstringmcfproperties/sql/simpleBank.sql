drop table customer2;

create table customer2 (
  SSN char(9),
  lastNAme char(20),
  firstname char(20),
  address1 char (20),
  address2 char(20),
  city char(10),
  state char(10),
  zipcode char(5),
  savingsbalance integer ,
  checkingbalance integer );


insert into customer2 values ('123456789', 'Smith', 'Rob', '1111', 'First Street', 'San Jose', 'CA', '12345', 5123, 1234);

