DROP TABLE Primitive_Types cascade constraint;
DROP TABLE Review cascade constraint;
DROP TABLE ProjectPerson cascade constraint;
DROP TABLE Project cascade constraint;
DROP TABLE Insurance cascade constraint;
DROP TABLE Person cascade constraint;
DROP TABLE Department cascade constraint;
DROP TABLE Company cascade constraint;

CREATE TABLE Company
(
    companyId INT PRIMARY KEY,
    name      VARCHAR2(32)        NULL,
    founded   DATE                NULL,
    address   VARCHAR2(100)       NULL
);

CREATE TABLE Department
(
    deptId          INT           PRIMARY KEY,
    name            VARCHAR2(32)  NULL,
    companyId       INT           NULL,
    employeeOfMonth INT           NULL,
    FOREIGN KEY (companyId)       REFERENCES Company (companyId)
);

CREATE TABLE Person
(
    personId      INT                 PRIMARY KEY,
    discriminator CHAR(1)             NOT NULL,
    firstName     VARCHAR2(32)        NULL,
    lastName      VARCHAR2(32)        NULL,
    middleName    VARCHAR2(32)        NULL,
    birthDate     DATE                NULL,
    address       VARCHAR2(100)       NULL,
    hireDate      DATE                NULL,
    weeklyhours   FLOAT               NOT NULL,
    deptId        INT                 NULL,
    fundingDeptId INT                 NULL,
    mgrId         INT                 NULL,
    mentorId      INT                 NULL,
    hrAdvisorId   INT                 NULL,
    wage          FLOAT               NULL,
    salary        FLOAT               NULL,
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
    insId                INT               PRIMARY KEY, 
    discriminator        CHAR(1)           NOT NULL,
    carrier              VARCHAR2(32)      NULL,
    personId             INT               NULL,
    planType             CHAR(3)           NULL,
    lifetimeOrthoBenefit FLOAT             NULL,
    FOREIGN KEY          (personId)        REFERENCES Person (personId),
    UNIQUE (personId)
);

CREATE TABLE Project
(
    projId INT          PRIMARY KEY,
    name   VARCHAR2(32) NULL,
    budget NUMBER(15,2) NULL        
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
    id               INT                  PRIMARY KEY,
    boolean_not_null DECIMAL(1)   /*BIT*/ NOT NULL,
    boolean_null     DECIMAL(1)   /*BIT*/ NULL,
    byte_not_null    SMALLINT /*TINYINT*/ NOT NULL,
    byte_null        SMALLINT /*TINYINT*/ NULL,
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
    date_null        DATE                 NULL,
    string_null      VARCHAR2(100)        NULL,
    big_decimal      DECIMAL(30,10)       NULL,
    big_integer      DECIMAL(30)          NULL,
    Primitive_Types  INT                  NULL
);

COMMIT;
