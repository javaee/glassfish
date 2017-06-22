Drop table O_Customer;

CREATE TABLE O_Customer (id integer , description char(50));

Drop table DESTROYED_INFO;

CREATE TABLE DESTROYED_INFO (test_name varchar(20), pool_name varchar(30), num_des_count integer);

insert into DESTROYED_INFO values ('test0', 'jdbc-unshareable-pool', 5);
insert into DESTROYED_INFO values ('test2', 'jdbc-unshareable-pool', 5);
insert into DESTROYED_INFO values ('test3', 'jdbc-shareable-pool', 5);
insert into DESTROYED_INFO values ('test4', 'jdbc-shareable-pool', 5);
insert into DESTROYED_INFO values ('test5', 'jdbc-unshareable-pool', 5);
insert into DESTROYED_INFO values ('test6', 'jdbc-unshareable-pool', 5);
insert into DESTROYED_INFO values ('test7', 'jdbc-shareable-pool', 1);
insert into DESTROYED_INFO values ('test8', 'jdbc-shareable-pool', 1);
insert into DESTROYED_INFO values ('test9', 'jdbc-shareable-pool', 1);
insert into DESTROYED_INFO values ('test9', 'jdbc-local-pool', 1);
insert into DESTROYED_INFO values ('test10', 'jdbc-shareable-pool', 1);
insert into DESTROYED_INFO values ('test10', 'jdbc-local-pool', 1);
insert into DESTROYED_INFO values ('test11', 'jdbc-shareable-pool', 1);
insert into DESTROYED_INFO values ('test11', 'jdbc-local-pool', 1);
insert into DESTROYED_INFO values ('test12', 'jdbc-shareable-pool', 1);
insert into DESTROYED_INFO values ('test12', 'jdbc-local-pool', 1);

drop table owner;

create table owner ( id integer, description char(50));

