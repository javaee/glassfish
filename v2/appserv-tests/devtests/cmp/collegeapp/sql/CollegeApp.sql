
drop table address CASCADE CONSTRAINTS;
create table address (addressId Number, street varchar2(255), code Number, city varchar2(255), state varchar2(255) ,constraint addr_ct primary key (addressId));

drop table account CASCADE CONSTRAINTS;
create table account (accountId varchar2(255), feesPaid Number, feesDue Number, dueDate Date ,constraint acc_ct primary key (accountId));


drop table dept CASCADE CONSTRAINTS;
create table dept (deptId Number, deptName Varchar2(255) ,constraint dept_ct primary key (deptId));

drop table student CASCADE CONSTRAINTS;

create table student (studentId Number, StudentName varchar2(255), deptId Number, AddressId Number, AccountId Varchar2(255), constraint st_c primary key (studentId),
FOREIGN KEY (deptId) REFERENCES dept (deptId),
FOREIGN KEY (AddressId) REFERENCES address (addressId),
FOREIGN KEY (AccountId) REFERENCES account (accountId)
);

drop table course CASCADE CONSTRAINTS;
create table course (courseId Number, deptId Number, courseName Varchar2(255), sylabus BLOB ,constraint course_ct primary key (courseId),
FOREIGN KEY (deptId) REFERENCES dept (deptId)
);
 
drop table stud_course CASCADE CONSTRAINTS;
create table stud_course (courseId Number, studentId Number,
constraint stuc_c primary key (courseId,studentId),
FOREIGN KEY (courseId) REFERENCES course (courseId),
FOREIGN KEY (studentId) REFERENCES student (studentId)
);

exit;
