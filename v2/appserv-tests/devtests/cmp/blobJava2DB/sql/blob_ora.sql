drop table MYBLOB;

create table MYBLOB (
	ID 	int primary key,
	NAME	VARCHAR2(100),
	BLB	RAW(255),
	BYTEBLB	RAW(255),
	BYTEBLB2	RAW(255)
);

commit;
insert into MYBLOB values (
	1,
	'AAA',
	'4444444444',
	'7777777777',
	'9999999999');

commit;
