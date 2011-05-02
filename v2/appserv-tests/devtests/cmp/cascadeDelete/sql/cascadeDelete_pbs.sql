DROP TABLE D;
DROP TABLE C;
DROP TABLE B;
DROP TABLE A;


CREATE TABLE A
(
    id INT          PRIMARY KEY,
    name   VARCHAR2(32) NULL
);

CREATE TABLE B
(
    id     INT          PRIMARY KEY,
    name   VARCHAR2(32) NULL,
    aId    INT          NULL,
    FOREIGN KEY (aId)   REFERENCES A (id)
);

CREATE TABLE C
(
    id   INT          PRIMARY KEY,
    name VARCHAR2(32) NULL,
    aId   INT          NULL,
    bId   INT          NULL,
    FOREIGN KEY (aId)  REFERENCES A (id),
    FOREIGN KEY (bId)  REFERENCES B (id),
    UNIQUE (bId)
);

CREATE TABLE D
(
    id   INT          PRIMARY KEY,
    name VARCHAR2(32) NULL,
    aId   INT          NULL,
    bId   INT          NULL,
    cId   INT          NULL,
    FOREIGN KEY (aId)  REFERENCES A (id),
    FOREIGN KEY (bId)  REFERENCES B (id),
    FOREIGN KEY (cId)  REFERENCES C (id)
);

commit;

quit;
