drop table customer2;

create table customer2 (
  SSN varchar2(9), 
  lastNAme varchar2(20), 
  firstname varchar2(20), 
  address1 varchar2 (20), 
  address2 varchar2(20), 
  city varchar2(10), 
  state varchar2(10), 
  zipcode varchar2(5), 
  savingsbalance number(10) ,
  checkingbalance number(10) );

insert into customer2 values ('123456789', 'Smith', 'Rob', '1111', 'First Street', 'San Jose', 'CA', '12345', 5123, 1234);

commit;
