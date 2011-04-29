drop table student;
drop table txn_log_table;
create table student (id varchar(255) primary key not null,  name varchar(255));
create table txn_log_table (localtid bigint, servername varchar(150), gtrid blob);
