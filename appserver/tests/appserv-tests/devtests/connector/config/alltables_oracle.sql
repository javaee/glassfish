-- droping all tables from the current user db
-- delete from user_tables;
select 'drop table', table_name,'cascade constraints;' from user_tables;
