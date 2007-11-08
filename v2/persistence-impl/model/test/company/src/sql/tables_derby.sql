ALTER TABLE Review DROP CONSTRAINT fk_rev_1;
ALTER TABLE Review DROP CONSTRAINT fk_rev_2;
ALTER TABLE ProjectPerson DROP CONSTRAINT fk_prj_pers_1;
ALTER TABLE ProjectPerson DROP CONSTRAINT fk_prj_pers_2;
ALTER TABLE Insurance DROP CONSTRAINT fk_ins_1;
ALTER TABLE Person DROP CONSTRAINT fk_pers_1;
ALTER TABLE Person DROP CONSTRAINT fk_pers_2;
ALTER TABLE Person DROP CONSTRAINT fk_pers_3;
ALTER TABLE Person DROP CONSTRAINT fk_pers_4;
ALTER TABLE Person DROP CONSTRAINT fk_pers_5;
ALTER TABLE Department DROP CONSTRAINT fk_dept_1;
ALTER TABLE Department DROP CONSTRAINT fk_dept_2;
DROP TABLE Primitive_Types;
DROP TABLE Review;
DROP TABLE ProjectPerson;
DROP TABLE Project;
DROP TABLE Insurance;
DROP TABLE Person;
DROP TABLE Department;
DROP TABLE Company;

CREATE TABLE Company
(
    companyId INT PRIMARY KEY    NOT NULL,
    name      VARCHAR(32),
    founded   TIMESTAMP,
    address   VARCHAR(100)
);

CREATE TABLE Department
(
    deptId          INT          PRIMARY KEY NOT NULL,
    name            VARCHAR(32),
    companyId       INT,
    employeeOfMonth INT,
    CONSTRAINT fk_dept_1 FOREIGN KEY (companyId) REFERENCES Company (companyId)
);

CREATE TABLE Person
(
    personId      INT          PRIMARY KEY NOT NULL,
    discriminator CHAR(1)      NOT NULL,
    firstName     VARCHAR(32),
    lastName      VARCHAR(32),
    middleName    VARCHAR(32),
    birthDate     TIMESTAMP,
    address       VARCHAR(100),
    hireDate      TIMESTAMP,
    weeklyhours   FLOAT        NOT NULL,
    deptId        INT,
    fundingDeptId INT,
    mgrId         INT,
    mentorId      INT,
    hrAdvisorId   INT,
    wage          FLOAT,
    salary        FLOAT,
    CONSTRAINT fk_pers_1 FOREIGN KEY (deptId)         REFERENCES Department (deptId),
    CONSTRAINT fk_pers_2 FOREIGN KEY (fundingDeptId)  REFERENCES Department (deptId)
);

ALTER TABLE Person
    ADD CONSTRAINT fk_pers_3 FOREIGN KEY (mgrId)           REFERENCES Person (personId);
ALTER TABLE Person
    ADD CONSTRAINT fk_pers_4 FOREIGN KEY (mentorId)        REFERENCES Person (personId);
ALTER TABLE Person
    ADD CONSTRAINT fk_pers_5 FOREIGN KEY (hrAdvisorId)     REFERENCES Person (personId);

ALTER TABLE Department
    ADD CONSTRAINT fk_dept_2 FOREIGN KEY (employeeOfMonth) REFERENCES Person (personId);

CREATE TABLE Insurance
(
    insId                INT          PRIMARY KEY NOT NULL,
    discriminator        CHAR(1)      NOT NULL,
    carrier              VARCHAR(32),
    personId             INT,
    planType             CHAR(3),
    lifetimeOrthoBenefit FLOAT,
    CONSTRAINT fk_ins_1 FOREIGN KEY (personId)  REFERENCES Person (personId)
);

CREATE TABLE Project
(
    projId INT          PRIMARY KEY NOT NULL,
    name   VARCHAR(32),
    budget NUMERIC(15,2)
);

CREATE TABLE ProjectPerson
(
    projId    INT NOT NULL,
    personId  INT NOT NULL,
    CONSTRAINT fk_prj_pers_1 FOREIGN KEY (projId)  REFERENCES Project (projId),
    CONSTRAINT fk_prj_pers_2 FOREIGN KEY (personId)  REFERENCES Person (personId)
);

CREATE TABLE Review
(
    projId    INT NOT NULL,
    personId  INT NOT NULL,
    CONSTRAINT fk_rev_1 FOREIGN KEY (projId)  REFERENCES Project (projId),
    CONSTRAINT fk_rev_2 FOREIGN KEY (personId)  REFERENCES Person (personId)
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
    date_null        TIMESTAMP,
    string_null      VARCHAR(100),
    big_decimal      DECIMAL(30,10),
    big_integer      BIGINT,
    Primitive_Types  INT
);

commit;
