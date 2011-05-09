drop table MYBLOB;

create table MYBLOB (
	ID 	int primary key NOT NULL,
	NAME	VARCHAR(100),
	BLB	BLOB,
	BYTEBLB	BLOB,
	BYTEBLB2	BLOB
);

insert into MYBLOB values (
	1,
	'AAA',
	CAST(X'4444444444' as BLOB),
	CAST(X'7777777777' as BLOB),
	CAST(X'9999999999' as BLOB));

