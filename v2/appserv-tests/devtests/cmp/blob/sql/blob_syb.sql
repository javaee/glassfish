drop table MYBLOB
go

create table MYBLOB (
	ID 	INT primary key NOT NULL,
	NAME	VARCHAR(100),
	BLB	IMAGE,
	BYTEBLB	IMAGE,
	BYTEBLB2	IMAGE
)
go

insert into MYBLOB values (
	1,
	'AAA',
	'4444444444',
	'7777777777',
	'9999999999')

go

