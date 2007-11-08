drop table customer1;

create table customer1 (
  SSN varchar2(9), 
  lastNAme varchar2(20), 
  firstname varchar2(20), 
  address1 varchar2 (20), 
  address2 varchar2(20), 
  city varchar2(10), 
  state varchar2(10), 
  zipcode varchar2(5), 
  balance decimal(10,2) );

insert into customer1 values ('123456789', 'Smith', 'Rob', '1111', 'First Street', 'San Jose', 'CA', '12345', 123);

