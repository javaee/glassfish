drop table myblob;

create table myblob (
	id 	int primary key NOT NULL,
	name	VARCHAR(100),
	blb	BLOB,
	byteblb	BLOB,
	byteblb2	BLOB
);

commit;
insert into myblob values (
	1,
	'AAA',
	CAST('4444444444' as BLOB),
	CAST('7777777777' as BLOB),
	CAST('9999999999' as BLOB));

commit;
