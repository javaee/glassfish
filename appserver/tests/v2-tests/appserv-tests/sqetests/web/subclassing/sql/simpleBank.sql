drop table customer2;

CREATE TABLE customer2 (
	SSN VARCHAR(9),
	lastName VARCHAR(24),
	firstname VARCHAR(20),
	address1 varchar(20),
	address2 varchar(20),
	city varchar(10) ,
	state varchar(10),
	zipcode varchar(5),
	savingsbalance numeric(10),
	checkingbalance numeric(10)	
);

insert into customer2 values ('123456789', 'Smith', 'Rob', '1111', 'First Street', 'San Jose', 'CA', '12345', 5123, 1234);





