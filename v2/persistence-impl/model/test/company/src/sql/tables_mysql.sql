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
    name      VARCHAR(32)         NULL,
    founded   DATE                NULL,
    address   VARCHAR(100)
) TYPE=INNODB;

CREATE TABLE Department
(
    deptId          INT           PRIMARY KEY,
    name            VARCHAR(32)   NULL,
    companyId       INT           NULL,
    employeeOfMonth INT           NULL
) TYPE=INNODB;

CREATE TABLE Person
(
    personId      INT                 PRIMARY KEY,
    discriminator CHAR(1)             NOT NULL,
    firstName     VARCHAR(32)         NULL,
    lastName      VARCHAR(32)         NULL,
    middleName    VARCHAR(32)         NULL,
    birthDate     DATE                NULL,
    address       VARCHAR(100)        NULL,
    hireDate      DATE                NULL,
    weeklyhours   FLOAT               NOT NULL,
    deptId        INT                 NULL,
    fundingDeptId INT                 NULL,
    mgrId         INT                 NULL,
    mentorId      INT                 NULL,
    hrAdvisorId   INT                 NULL,
    wage          FLOAT               NULL,
    salary        FLOAT               NULL,
    UNIQUE(mentorId)
) TYPE=INNODB;

ALTER TABLE Department 
    ADD CONSTRAINT FK_Dept_2 FOREIGN KEY (employeeOfMonth) REFERENCES Person (personId);

CREATE TABLE Insurance
(
    insId                INT               PRIMARY KEY, 
    discriminator        CHAR(1)           NOT NULL,
    carrier              VARCHAR(32)       NULL,
    personId             INT               NULL,
    planType             CHAR(3)           NULL,
    lifetimeOrthoBenefit FLOAT             NULL,
    UNIQUE (personId)
) TYPE=INNODB;

CREATE TABLE Project
(
    projId INT           PRIMARY KEY,
    name   VARCHAR(32)   NULL,
    budget DECIMAL(15,2) NULL        
) TYPE=INNODB;

CREATE TABLE ProjectPerson
(
    projId   INT           NOT NULL,  
    personId INT           NOT NULL,
) TYPE=INNODB;

CREATE TABLE Review
(
    projId   INT           NOT NULL,
    personId INT           NOT NULL,
) TYPE=INNODB;

CREATE TABLE Primitive_Types
(
    id               INT                  PRIMARY KEY,
    boolean_not_null BIT                  NOT NULL,
    boolean_null     BIT                  NULL,
    byte_not_null    TINYINT              NOT NULL,
    byte_null        TINYINT              NULL,
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
    string_null      VARCHAR(100)         NULL,
    big_decimal      DECIMAL(30,10)       NULL,
    big_integer      DECIMAL(30)          NULL,
    Primitive_Types  INT                  NULL
) TYPE=INNODB;

COMMIT;
