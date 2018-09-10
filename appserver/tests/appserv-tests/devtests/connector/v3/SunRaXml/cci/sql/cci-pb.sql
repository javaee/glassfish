drop table coffee;

drop PROCEDURE COUNTCOFFEE;
drop PROCEDURE INSERTCOFFEE;

create table coffee (name varchar(32), qty integer);

CREATE PROCEDURE countCoffee ( OUT ocount INTEGER )
PARAMETER STYLE JAVA
LANGUAGE JAVA
READS SQL DATA
EXTERNAL NAME 'SampleExternalMethods.countCoffee';

CREATE PROCEDURE insertCoffee (IN name VARCHAR(32), IN qty INTEGER)
PARAMETER STYLE JAVA
LANGUAGE JAVA
MODIFIES SQL DATA
EXTERNAL NAME 'SampleExternalMethods.insertCoffee';
                                                                

