drop table myblob;

create table myblob (
	id 	int primary key,
	name	VARCHAR(100),
	blb	BLOB,
	byteblb	BLOB,
	byteblb2	BLOB
);

commit;
insert into myblob values (
	1,
	'AAA',
	CAST('4444444444' AS BLOB),
	CAST('7777777777' AS BLOB),
	CAST('9999999999' AS BLOB));

commit;
