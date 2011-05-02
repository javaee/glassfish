drop table COMPOSITEINTSTRINGBEANTABLE
go

create table COMPOSITEINTSTRINGBEANTABLE (
	ID INTEGER not null, 
	NAME VARCHAR(255) not null, 
	SALARY double precision not null, 
	constraint PK_COMPOSITEINTSTRINGBEANTABL primary key (ID, NAME)
)

go
