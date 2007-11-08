create table ReadOnlyStudent 
(studentid varchar(15) constraint pk_ReadOnlystudent primary key not null,  
name varchar(36));

insert into ReadOnlyStudent
values ('student0', 'Student_ID_0');
insert into ReadOnlyStudent
values ('student1', 'Student_ID_1');
insert into ReadOnlyStudent
values ('student2', 'Student_ID_2');
insert into ReadOnlyStudent
values ('student3', 'Student_ID_3');
insert into ReadOnlyStudent
values ('student4', 'Student_ID_4');
insert into ReadOnlyStudent
values ('student5', 'Student_ID_5');


create table ReadOnlyCourse 
(courseid varchar(15) constraint pk_ReadOnlycourse primary key not null,  
name varchar(36));

insert into ReadOnlyCourse
values ('course0', 'Course_ID_0');
insert into ReadOnlyCourse
values ('course1', 'Course_ID_1');
insert into ReadOnlyCourse
values ('course2', 'Course_ID_2');
insert into ReadOnlyCourse
values ('course3', 'Course_ID_3');
insert into ReadOnlyCourse
values ('course4', 'Course_ID_4');
insert into ReadOnlyCourse
values ('course5', 'Course_ID_5');

create table ReadOnlyEnrollment
(studentid varchar(15),  
courseid varchar(15),  
constraint fk_studentid
foreign key (studentid)
references ReadOnlystudent(studentid),
constraint fk_courseid
foreign key (courseid)
references ReadOnlycourse(courseid));

