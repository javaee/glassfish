DROP TABLE D;
DROP TABLE C;
DROP TABLE B;
DROP TABLE A;


CREATE TABLE A
(
    id INT          PRIMARY KEY,
    name   VARCHAR(32) 
);

CREATE TABLE B
(
    id     INT          PRIMARY KEY,
    name   VARCHAR(32) ,
    aId    INT          ,
    FOREIGN KEY (aId)   REFERENCES A (id)
);

CREATE TABLE C
(
    id   INT          PRIMARY KEY,
    name VARCHAR(32) ,
    aId   INT          ,
    bId   INT          ,
    FOREIGN KEY (aId)  REFERENCES A (id),
    FOREIGN KEY (bId)  REFERENCES B (id)
);

CREATE TABLE D
(
    id   INT          PRIMARY KEY,
    name VARCHAR(32) ,
    aId   INT          ,
    bId   INT          ,
    cId   INT          ,
    FOREIGN KEY (aId)  REFERENCES A (id),
    FOREIGN KEY (bId)  REFERENCES B (id),
    FOREIGN KEY (cId)  REFERENCES C (id)
);

commit;

quit;
