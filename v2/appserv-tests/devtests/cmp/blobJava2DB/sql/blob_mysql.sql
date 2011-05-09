drop table MYBLOB;

create table MYBLOB (
	ID 	int primary key,
	NAME	VARCHAR(100),
	BLB	BLOB(255),
	BYTEBLB	BLOB(255),
	BYTEBLB2	BLOB(255)
) ENGINE=InnoDB;

commit;
insert into MYBLOB values (
	1,
	'AAA',
	'4444444444',
	'7777777777',
	'9999999999');

commit;
