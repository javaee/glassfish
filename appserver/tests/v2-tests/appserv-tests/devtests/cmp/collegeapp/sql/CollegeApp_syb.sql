drop table STUD_COURSE
drop table COURSE
drop table STUDENT
drop table DEPT
drop table ACCOUNT
drop table ADDRESS
go

create table ADDRESS (
    ADDRESSID NUMERIC(38) not null,
    STREET VARCHAR(255),
    CODE NUMERIC(38),
    CITY VARCHAR(255),
    STATE VARCHAR(255),
    constraint ADDR_CT primary key (ADDRESSID)
)

create table ACCOUNT (
    ACCOUNTID VARCHAR(255) not null,
    FEESPAID NUMERIC(38,2),
    FEESDUE NUMERIC(38,2),
    DUEDATE DATETIME,
    constraint ACC_CT primary key (ACCOUNTID)
)

create table DEPT (
    DEPTID NUMERIC(38) not null,
    DEPTNAME VARCHAR(255),
    constraint DEPT_CT primary key (DEPTID)
)

create table STUDENT (
    STUDENTID NUMERIC(38) not null,
    STUDENTNAME VARCHAR(255),
    DEPTID NUMERIC(38),
    ADDRESSID NUMERIC(38),
    ACCOUNTID VARCHAR(255),
    constraint ST_CT primary key (STUDENTID),
    foreign key (DEPTID) references DEPT (DEPTID),
    foreign key (ADDRESSID) references ADDRESS (ADDRESSID),
    foreign key (ACCOUNTID) references ACCOUNT (ACCOUNTID)
)

create table COURSE (
    COURSEID NUMERIC(38) not null,
    DEPTID NUMERIC(38),
    COURSENAME VARCHAR(255),
    SYLABUS IMAGE,
    constraint COURSE_CT primary key (COURSEID),
    foreign key (DEPTID) references DEPT (DEPTID)
)

create table STUD_COURSE (
    COURSEID NUMERIC(38) not null,
    STUDENTID NUMERIC(38) not null,
    constraint STCO_CT primary key (COURSEID, STUDENTID),
    foreign key (COURSEID) references COURSE (COURSEID),
    foreign key (STUDENTID) references STUDENT (STUDENTID)
)

go


