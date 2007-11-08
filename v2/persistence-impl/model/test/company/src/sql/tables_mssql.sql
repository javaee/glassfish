ALTER TABLE Department DROP CONSTRAINT fk_dept_2
go
DROP TABLE Primitive_Types
go
DROP TABLE Review
go
DROP TABLE ProjectPerson
go
DROP TABLE Project
go
DROP TABLE Insurance
go
DROP TABLE Person
go
DROP TABLE Department
go
DROP TABLE Company
go

CREATE TABLE Company
(
    companyId INT PRIMARY KEY,
    name      VARCHAR(32)        NULL,
    founded   DATETIME           NULL,
    address   VARCHAR(100)
)
go

CREATE TABLE Department
(
    deptId          INT           PRIMARY KEY,
    name            VARCHAR(32)   NULL,
    companyId       INT           NULL,
    employeeOfMonth INT           NULL,
    CONSTRAINT fk_dept_1 FOREIGN KEY (companyId) REFERENCES Company (companyId)
)
go

CREATE TABLE Person
(
    personId      INT                 PRIMARY KEY,
    discriminator CHAR(1)             NOT NULL,
    firstName     VARCHAR(32)         NULL,
    lastName      VARCHAR(32)         NULL,
    middleName    VARCHAR(32)         NULL,
    birthDate     DATETIME            NULL,
    address       VARCHAR(100)        NULL,
    hireDate      DATETIME            NULL,
    weeklyhours   FLOAT               NOT NULL,
    deptId        INT                 NULL,
    fundingDeptId INT                 NULL,
    mgrId         INT                 NULL,
    mentorId      INT                 NULL,
    hrAdvisorId   INT                 NULL,
    wage          FLOAT               NULL,
    salary        FLOAT               NULL,
    CONSTRAINT fk_pers_1 FOREIGN KEY (deptId)        REFERENCES Department (deptId),
    CONSTRAINT fk_pers_2 FOREIGN KEY (fundingDeptId) REFERENCES Department (deptId),
    CONSTRAINT fk_pers_3 FOREIGN KEY (mgrId)         REFERENCES Person (personId),
    CONSTRAINT fk_pers_4 FOREIGN KEY (mentorId)      REFERENCES Person (personId),
    CONSTRAINT fk_pers_5 FOREIGN KEY (hrAdvisorId)   REFERENCES Person (personId),
    CONSTRAINT uk_pers_1 UNIQUE(mentorId)
)
go

ALTER TABLE Department 
    ADD CONSTRAINT fk_dept_2 FOREIGN KEY (employeeOfMonth) REFERENCES Person (personId)
go

CREATE TABLE Insurance
(
    insId                INT               PRIMARY KEY, 
    discriminator        CHAR(1)           NOT NULL,
    carrier              VARCHAR(32)       NULL,
    personId             INT               NULL,
    planType             CHAR(3)           NULL,
    lifetimeOrthoBenefit FLOAT             NULL,
    CONSTRAINT fk_ins_1 FOREIGN KEY (personId) REFERENCES Person (personId),
    CONSTRAINT uk_ins_1 UNIQUE (personId)
)
go

CREATE TABLE Project
(
    projId INT           PRIMARY KEY,
    name   VARCHAR(32)   NULL,
    budget DECIMAL(15,2) NULL        
)
go

CREATE TABLE ProjectPerson
(
    projId   INT            NOT NULL,  
    personId INT            NOT NULL,
    CONSTRAINT fk_prjpers_1 FOREIGN KEY (projId) REFERENCES Project (projId),
    CONSTRAINT fk_prjpers_2 FOREIGN KEY (personId) REFERENCES Person (personId)
)
go

CREATE TABLE Review
(
    projId   INT           NOT NULL,
    personId INT           NOT NULL,
    CONSTRAINT fk_review_1 FOREIGN KEY (projId) REFERENCES Project (projId),
    CONSTRAINT fk_review_2 FOREIGN KEY (personId) REFERENCES Person (personId)
)
go

CREATE TABLE Primitive_Types
(
    id               INT                  PRIMARY KEY,
    boolean_not_null BIT                  NOT NULL,
    boolean_null     BIT                  NULL,
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
    char_not_null    CHAR                 NULL,
    char_null        CHAR                 NULL,
    date_null        DATETIME             NULL,
    string_null      VARCHAR(100)         NULL,
    big_decimal      DECIMAL(30,10)       NULL,
    big_integer      BIGINT               NULL,
    Primitive_Types  INT                  NULL
)
go

COMMIT
go
