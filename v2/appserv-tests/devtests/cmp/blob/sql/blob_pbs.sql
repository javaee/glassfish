drop table myblob;

create table myblob (
	id 	int primary key,
	name	VARCHAR2(100),
	blb	BLOB(10k),
	byteblb	BLOB(10k),
	byteblb2	BLOB(10k)
);

commit;
insert into myblob values (
	1,
	'AAA',
	CAST('4444444444' as BLOB),
	CAST('7777777777' as BLOB),
	CAST('9999999999' as BLOB));

commit;
