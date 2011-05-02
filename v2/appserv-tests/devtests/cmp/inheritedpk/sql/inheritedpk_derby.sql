DROP TABLE C;
DROP TABLE B;
DROP TABLE A;


CREATE TABLE A
(
    id     INT          PRIMARY KEY NOT NULL,
    lastName  VARCHAR(32) ,
    firstName VARCHAR(32) ,
    hireDate  DATE         ,
    birthDate DATE         ,
    salary    FLOAT        NOT NULL
);

CREATE TABLE B
(
    id DATE          PRIMARY KEY NOT NULL,
    name   VARCHAR(32) 
);

CREATE TABLE C
(
    id INT          PRIMARY KEY NOT NULL,
    name   VARCHAR(32) 
);

