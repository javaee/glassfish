drop table COPYTEST;

create table COPYTEST (
	id 	int primary key,
	name	VARCHAR2(100),
	mydate	DATE,
	blb	RAW(255)
);

commit;
