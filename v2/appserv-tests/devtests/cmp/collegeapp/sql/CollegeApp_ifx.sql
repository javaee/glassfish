drop table stud_course;
drop table course;
drop table student;
drop table dept;
drop table account;
drop table address;


create table address (
    addressId DECIMAL(32), 
    street VARCHAR(127), 
    code DECIMAL(32), 
    city VARCHAR(127), 
    state VARCHAR(127), 
    primary key (addressId)
);

create table account (
    accountId VARCHAR(127), 
    feesPaid DECIMAL(32,2), 
    feesDue DECIMAL(32,2), 
    dueDate DATE, 
    primary key (accountId)
);

create table dept (
    deptId DECIMAL(32), 
    deptName VARCHAR(127), 
    primary key (deptId)
);

create table student (
    studentId DECIMAL(32), 
    studentName VARCHAR(127), 
    deptId DECIMAL(32), 
    addressId DECIMAL(32), 
    accountId VARCHAR(127), 
    primary key (studentId),
    FOREIGN KEY (deptId) REFERENCES dept (deptId),
    FOREIGN KEY (addressId) REFERENCES address (addressId),
    FOREIGN KEY (accountId) REFERENCES account (accountId)
);

create table course (
    courseId DECIMAL(32), 
    deptId DECIMAL(32), 
    courseName VARCHAR(127), 
    sylabus BYTE, 
    primary key (courseId),
    FOREIGN KEY (deptId) REFERENCES dept (deptId)
);
 
create table stud_course (
    courseId DECIMAL(32), 
    studentId DECIMAL(32),
    primary key (courseId, studentId),
    FOREIGN KEY (courseId) REFERENCES course (courseId),
    FOREIGN KEY (studentId) REFERENCES student (studentId)
);

exit;
