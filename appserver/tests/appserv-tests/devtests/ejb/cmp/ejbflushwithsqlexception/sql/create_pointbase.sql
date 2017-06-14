Drop table A1;
Drop table A2;

CREATE TABLE A1 (
    id     char(3) not null constraint pk_A1_id primary key,
    name   char(5)
);

CREATE TABLE A2 (
    id     char(3) not null constraint pk_A2_id primary key,
    name   char(5)
);

