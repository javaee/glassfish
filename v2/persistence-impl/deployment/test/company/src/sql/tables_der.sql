DROP TABLE Primitive_Types;
DROP TABLE Review;
DROP TABLE ProjectPerson;
DROP TABLE Project;
DROP TABLE Insurance;
ALTER TABLE Department DROP CONSTRAINT FK_Dept_2;
DROP TABLE Department;
DROP TABLE Person;
DROP TABLE Company;


CREATE TABLE Company
(
    companyId INT PRIMARY KEY NOT NULL,
    name      VARCHAR(32),
    founded   DATE,
    address   VARCHAR(100)
);

CREATE TABLE Department
(
    deptId          INT           PRIMARY KEY NOT NULL,
    name            VARCHAR(32),
    companyId       INT,
    employeeOfMonth INT,
    FOREIGN KEY (companyId)       REFERENCES Company (companyId)
);

CREATE TABLE Person
(
    personId      INT                 PRIMARY KEY NOT NULL,
    discriminator CHAR(1)             NOT NULL,
    firstName     VARCHAR(32),
    lastName      VARCHAR(32),
    middleName    VARCHAR(32),
    birthDate     DATE,
    address       VARCHAR(100),
    hireDate      DATE,
    weeklyhours   FLOAT               NOT NULL,
    deptId        INT                 NOT NULL,
    fundingDeptId INT                 NOT NULL,
    mgrId         INT                 NOT NULL,
    mentorId      INT                 NOT NULL,
    hrAdvisorId   INT                 NOT NULL,
    wage          FLOAT,
    salary        FLOAT,
    FOREIGN KEY   (deptId)            REFERENCES Department (deptId), 
    FOREIGN KEY   (fundingDeptId)     REFERENCES Department (deptId),
    FOREIGN KEY   (mgrId)             REFERENCES Person (personId), 
    FOREIGN KEY   (mentorId)          REFERENCES Person (personId),
    FOREIGN KEY   (hrAdvisorId)       REFERENCES Person (personId),
    UNIQUE(mentorId)
);

ALTER TABLE Department 
    ADD CONSTRAINT FK_Dept_2 FOREIGN KEY (employeeOfMonth) REFERENCES Person (personId);

CREATE TABLE Insurance
(
    insId                INT               PRIMARY KEY NOT NULL,
    discriminator        CHAR(1)           NOT NULL,
    carrier              VARCHAR(32),
    personId             INT               NOT NULL,
    planType             CHAR(3),
    lifetimeOrthoBenefit FLOAT,
    FOREIGN KEY          (personId)        REFERENCES Person (personId),
    UNIQUE (personId)
);

CREATE TABLE Project
(
    projId INT          PRIMARY KEY NOT NULL,
    name   VARCHAR(32),
    budget DECIMAL(15, 2)
);

CREATE TABLE ProjectPerson
(
    projId   INT           NOT NULL,  
    personId INT           NOT NULL,
    FOREIGN KEY (projId)   REFERENCES Project (projId),
    FOREIGN KEY (personId) REFERENCES Person (personId)
);

CREATE TABLE Review
(
    projId   INT           NOT NULL,
    personId INT           NOT NULL,
    FOREIGN KEY (projId)   REFERENCES Project (projId),
    FOREIGN KEY (personId) REFERENCES Person (personId)
);

CREATE TABLE Primitive_Types
(
    id               INT                  PRIMARY KEY NOT NULL,
    boolean_not_null DECIMAL(1)           NOT NULL,
    boolean_null     DECIMAL(1),
    byte_not_null    SMALLINT             NOT NULL,
    byte_null        SMALLINT,
    short_not_null   SMALLINT             NOT NULL,
    short_null       SMALLINT,
    int_not_null     INT                  NOT NULL,
    int_null         INT,
    long_not_null    DECIMAL(20)          NOT NULL,
    long_null        DECIMAL(20),
    float_not_null   FLOAT                NOT NULL,
    float_null       FLOAT,
    double_not_null  DOUBLE PRECISION     NOT NULL,
    double_null      DOUBLE PRECISION,
    char_not_null    CHAR                 NOT NULL,
    char_null        CHAR,
    date_null        DATE,
    string_null      VARCHAR(100),
    big_decimal      DECIMAL(30,10),
    big_integer      DECIMAL(30),
    Primitive_Types  INT
);
