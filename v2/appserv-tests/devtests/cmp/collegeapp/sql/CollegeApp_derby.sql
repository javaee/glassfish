drop table stud_course;
drop table course;
drop table student;
drop table dept;
drop table account;
drop table address;


create table address (
    addressId NUMERIC(30) NOT NULL, 
    street VARCHAR(255), 
    code NUMERIC(30), 
    city VARCHAR(255), 
    state VARCHAR(255), 
    constraint addr_ct primary key (addressId)
);

create table account (
    accountId VARCHAR(255) NOT NULL, 
    feesPaid NUMERIC(30,2), 
    feesDue NUMERIC(30,2), 
    dueDate DATE, 
    constraint acc_ct primary key (accountId)
);

create table dept (
    deptId NUMERIC(30) NOT NULL, 
    deptName VARCHAR(255), 
    constraint dept_ct primary key (deptId)
);

create table student (
    studentId NUMERIC(30) NOT NULL, 
    studentName VARCHAR(255), 
    deptId NUMERIC(30), 
    addressId NUMERIC(30), 
    accountId VARCHAR(255), 
    constraint st_ct primary key (studentId),
    FOREIGN KEY (deptId) REFERENCES dept (deptId),
    FOREIGN KEY (addressId) REFERENCES address (addressId),
    FOREIGN KEY (accountId) REFERENCES account (accountId)
);

create table course (
    courseId NUMERIC(30) NOT NULL, 
    deptId NUMERIC(30), 
    courseName VARCHAR(255), 
    sylabus BLOB, 
    constraint course_ct primary key (courseId),
    FOREIGN KEY (deptId) REFERENCES dept (deptId)
);
 
create table stud_course (
    courseId NUMERIC(30) NOT NULL, 
    studentId NUMERIC(30) NOT NULL,
    constraint stco_ct primary key (courseId, studentId),
    FOREIGN KEY (courseId) REFERENCES course (courseId),
    FOREIGN KEY (studentId) REFERENCES student (studentId)
);

