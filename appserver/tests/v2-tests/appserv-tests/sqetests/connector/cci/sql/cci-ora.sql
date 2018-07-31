

drop table coffee;
drop procedure COUNTCOFFEE;
drop procedure INSERTCOFFEE;

create table coffee (name varchar(32), qty integer);

create or replace procedure COUNTCOFFEE (N OUT INTEGER) is
begin select count(*) into N from coffee;
end;
/

create or replace procedure INSERTCOFFEE (name IN VARCHAR2, qty IN INTEGER) is
begin insert into coffee values (name, qty);
end;
/
