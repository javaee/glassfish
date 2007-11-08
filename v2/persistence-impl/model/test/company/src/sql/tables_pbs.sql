DROP TABLE Primitive_Types cascade;
DROP TABLE Review cascade;
DROP TABLE ProjectPerson cascade;
DROP TABLE Project cascade;
DROP TABLE Insurance cascade;
DROP TABLE Person cascade;
DROP TABLE Department cascade;
DROP TABLE Company cascade;

CREATE TABLE Company
(
    companyId INT PRIMARY KEY,
    name      VARCHAR2(32)        NULL,
    founded   TIMESTAMP           NULL,
    address   VARCHAR2(100)
);

CREATE TABLE Department
(
    deptId          INT           PRIMARY KEY,
    name            VARCHAR2(32)  NULL,
    companyId       INT           NULL,
    employeeOfMonth INT           NULL
);

ALTER TABLE Department
    ADD CONSTRAINT FK_Dept_1 FOREIGN KEY (companyId) REFERENCES Company (companyId);

CREATE TABLE Person
(
    personId      INT                 PRIMARY KEY,
    discriminator CHAR(1)             NOT NULL,
    firstName     VARCHAR2(32)        NULL,
    lastName      VARCHAR2(32)        NULL,
    middleName    VARCHAR2(32)        NULL,
    birthDate     TIMESTAMP           NULL,
    address       VARCHAR2(100)       NULL,
    hireDate      TIMESTAMP           NULL,
    weeklyhours   FLOAT               NOT NULL,
    deptId        INT                 NULL,
    fundingDeptId INT                 NULL,
    mgrId         INT                 NULL,
    mentorId      INT                 NULL,
    hrAdvisorId   INT                 NULL,
    wage          FLOAT               NULL,
    salary        FLOAT               NULL
);

ALTER TABLE Person
    ADD CONSTRAINT FK_Person_1 FOREIGN KEY (deptId)      REFERENCES Department (deptId);
ALTER TABLE Person
    ADD CONSTRAINT FK_Person_2 FOREIGN KEY (mgrId)       REFERENCES Person (personId);
ALTER TABLE Person
    ADD CONSTRAINT FK_Person_3 FOREIGN KEY (mentorId)    REFERENCES Person (personId);
ALTER TABLE Person
    ADD CONSTRAINT FK_Person_4 FOREIGN KEY (hrAdvisorId) REFERENCES Person (personId);
ALTER TABLE Person
    ADD CONSTRAINT UK_Person_1 UNIQUE(mentorId);

ALTER TABLE Department 
    ADD CONSTRAINT FK_Dept_2 FOREIGN KEY (employeeOfMonth) REFERENCES Person (personId);

CREATE TABLE Insurance
(
    insId                INT               PRIMARY KEY, 
    discriminator        CHAR(1)           NOT NULL,
    carrier              VARCHAR2(32)      NULL,
    personId             INT               NULL,
    planType             CHAR(3)           NULL,
    lifetimeOrthoBenefit FLOAT             NULL
);

ALTER TABLE Insurance
    ADD CONSTRAINT FK_Insurance_1 FOREIGN KEY (personId) REFERENCES Person (personId);
ALTER TABLE Insurance
    ADD CONSTRAINT UK_Insurance_1 UNIQUE (personId);

CREATE TABLE Project
(
    projId INT          PRIMARY KEY,
    name   VARCHAR2(32) NULL,
    budget NUMBER(15,2) NULL        
);

CREATE TABLE ProjectPerson
(
    projId   INT         NOT NULL,  
    personId INT         NOT NULL
);

ALTER TABLE ProjectPerson
    ADD CONSTRAINT FK_ProjectPerson_1 FOREIGN KEY (projId) REFERENCES Project (projId);
ALTER TABLE ProjectPerson
    ADD CONSTRAINT FK_ProjectPerson_2 FOREIGN KEY (personId) REFERENCES Person (personId);

CREATE TABLE Review
(
    projId   INT         NOT NULL,
    personId INT         NOT NULL
);

ALTER TABLE Review
    ADD CONSTRAINT FK_Review_1 FOREIGN KEY (projId) REFERENCES Project (projId);
ALTER TABLE Review
    ADD CONSTRAINT FK_Review_2 FOREIGN KEY (personId) REFERENCES Person (personId);

CREATE TABLE Primitive_Types
(
    id               INT                  PRIMARY KEY,
    boolean_not_null DECIMAL(1)           NOT NULL,
    boolean_null     DECIMAL(1)           NULL,
    byte_not_null    SMALLINT             NOT NULL,
    byte_null        SMALLINT             NULL,
    short_not_null   SMALLINT             NOT NULL,
    short_null       SMALLINT             NULL,
    int_not_null     INT                  NOT NULL,
    int_null         INT                  NULL,
    long_not_null    DECIMAL(20)          NOT NULL,
    long_null        DECIMAL(20)          NULL,
    float_not_null   FLOAT                NOT NULL,
    float_null       FLOAT                NULL,
    double_not_null  DOUBLE PRECISION     NOT NULL,
    double_null      DOUBLE PRECISION     NULL,
    char_not_null    CHAR                 NOT NULL,
    char_null        CHAR                 NULL,
    date_null        TIMESTAMP            NULL,
    string_null      VARCHAR2(100)        NULL,
    big_decimal      DECIMAL(30,10)       NULL,
    big_integer      BIGINT               NULL,
    Primitive_Types  INT                  NULL
);

COMMIT;
