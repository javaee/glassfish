Drop table HandleStudent;
create table HandleStudent
(studentid varchar(3) primary key not null,  
name varchar(36));

Drop table HandleCourse;
create table HandleCourse
(courseid varchar(3) primary key not null,  
name varchar(36));

Drop table HandleEnrollment;
create table HandleEnrollment
(courseid varchar(3),  
studentid varchar(36));

