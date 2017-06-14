drop table customer1;

create table customer1 (
  SSN varchar(9), 
  lastNAme varchar(20), 
  firstname varchar(20), 
  address1 varchar(20), 
  address2 varchar(20), 
  city varchar(10), 
  state varchar(10), 
  zipcode varchar(5), 
  balance decimal(10,2) );

insert into customer1 values ('123456789', 'Smith', 'Rob', '1111', 'First Street', 'San Jose', 'CA', '12345', 123);

