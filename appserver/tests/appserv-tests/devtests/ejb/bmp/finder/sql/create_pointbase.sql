create table FinderStudent 
(studentid varchar(3) constraint pk_FinderStudent primary key not null,  
name varchar(36));

insert into FinderStudent
values ('123', 'Sal Jones');
insert into FinderStudent
values ('221', 'Alice Smith');
insert into FinderStudent
values ('388', 'Elizabeth Willis');
insert into FinderStudent
values ('456', 'Joe Smith');


create table FinderCourse 
(courseid varchar(3) constraint pk_FinderCourse primary key not null,  
name varchar(36));

insert into FinderCourse
values ('999', 'Advanced Java Programming');
insert into FinderCourse
values ('111', 'J2EE for Smart People');
insert into FinderCourse
values ('333', 'XML Made Easy');
insert into FinderCourse
values ('777', 'An Introduction to Java Programming');

create table FinderEnrollment
(studentid varchar(3),  
courseid varchar(3),  
constraint fk_studentid
foreign key (studentid)
references FinderStudent(studentid),
constraint fk_courseid
foreign key (courseid)
references FinderCourse(courseid));

insert into FinderEnrollment
values ('123', '777');
insert into FinderEnrollment
values ('221', '777');
insert into FinderEnrollment
values ('388', '777');
insert into FinderEnrollment
values ('456', '777');
