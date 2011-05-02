drop table MYBLOB cascade;

create table MYBLOB (
	ID 	int primary key,
	NAME	VARCHAR(100),
	BLB	BYTEA,
	BYTEBLB	BYTEA,
	BYTEBLB2	BYTEA
) ;

commit;
insert into MYBLOB values (
	1,
	'AAA',
	'4444444444',
	'7777777777',
	'9999999999');

commit;
