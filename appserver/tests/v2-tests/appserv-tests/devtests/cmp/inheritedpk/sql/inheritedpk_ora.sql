DROP TABLE D;
DROP TABLE C;
DROP TABLE B;
DROP TABLE A;


CREATE TABLE A
(
    id     INT          PRIMARY KEY,
    lastName  VARCHAR2(32) NULL,
    firstName VARCHAR2(32) NULL,
    hireDate  DATE         NULL,
    birthDate DATE         NULL,
    salary    FLOAT        NOT NULL
);

CREATE TABLE B
(
    id DATE          PRIMARY KEY,
    name   VARCHAR2(32) NULL
);

CREATE TABLE C
(
    id INT          PRIMARY KEY,
    name   VARCHAR2(32) NULL
);

commit;

quit;
