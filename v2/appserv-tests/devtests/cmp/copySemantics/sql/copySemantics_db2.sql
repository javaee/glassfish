drop table COPYTEST;

create table COPYTEST (
	id 	int primary key NOT NULL,
	name	VARCHAR(100),
	mydate	TIMESTAMP,
	blb	BLOB
);

commit;
