drop table COPYTEST;

create table COPYTEST (
	id 	int primary key,
	name	VARCHAR(100),
	mydate	DATE,
	blb	BLOB
);

commit;
