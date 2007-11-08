create table student 
(studentid varchar(3) constraint pk_student primary key,  
name varchar(36));

insert into student
values ('123', 'Sal Jones');
insert into student
values ('221', 'Alice Smith');
insert into student
values ('388', 'Elizabeth Willis');
insert into student
values ('456', 'Joe Smith');


create table course 
(courseid varchar(3) constraint pk_course primary key,  
name varchar(36));

insert into course
values ('999', 'Advanced Java Programming');
insert into course
values ('111', 'J2EE for Smart People');
insert into course
values ('333', 'XML Made Easy');
insert into course
values ('777', 'An Introduction to Java Programming');

create table enrollment
(studentid varchar(3),  
courseid varchar(3),  
constraint fk_studentid
foreign key (studentid)
references student(studentid),
constraint fk_courseid
foreign key (courseid)
references course(courseid));

