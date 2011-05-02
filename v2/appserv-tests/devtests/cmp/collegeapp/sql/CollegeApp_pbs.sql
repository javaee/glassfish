drop table stud_course;
drop table course;
drop table student;
drop table dept;
drop table account;
drop table address;


create table address (
    addressId NUMBER(38), 
    street VARCHAR2(255), 
    code NUMBER(38), 
    city VARCHAR2(255), 
    state VARCHAR2(255), 
    constraint addr_ct primary key (addressId)
);

create table account (
    accountId VARCHAR2(255), 
    feesPaid NUMBER(38,2), 
    feesDue NUMBER(38,2), 
    dueDate TIMESTAMP, 
    constraint acc_ct primary key (accountId)
);

create table dept (
    deptId NUMBER(38), 
    deptName VARCHAR2(255), 
    constraint dept_ct primary key (deptId)
);

create table student (
    studentId NUMBER(38), 
    studentName VARCHAR2(255), 
    deptId NUMBER(38), 
    addressId NUMBER(38), 
    accountId VARCHAR2(255), 
    constraint st_ct primary key (studentId),
    FOREIGN KEY (deptId) REFERENCES dept (deptId),
    FOREIGN KEY (addressId) REFERENCES address (addressId),
    FOREIGN KEY (accountId) REFERENCES account (accountId)
);

create table course (
    courseId NUMBER(38), 
    deptId NUMBER(38), 
    courseName VARCHAR2(255), 
    sylabus BLOB (10k), 
    constraint course_ct primary key (courseId),
    FOREIGN KEY (deptId) REFERENCES dept (deptId)
);
 
create table stud_course (
    courseId NUMBER(38), 
    studentId NUMBER(38),
    constraint stco_ct primary key (courseId, studentId),
    FOREIGN KEY (courseId) REFERENCES course (courseId),
    FOREIGN KEY (studentId) REFERENCES student (studentId)
);

exit;
