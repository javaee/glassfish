DROP TABLE D cascade;
DROP TABLE C cascade;
DROP TABLE B cascade;
DROP TABLE A cascade;


CREATE TABLE A
(
    ID INT          PRIMARY KEY,
    NAME   VARCHAR(32) NULL
);

CREATE TABLE B
(
    ID     INT          PRIMARY KEY,
    NAME   VARCHAR(32) NULL,
    AID    INT          NULL,
    FOREIGN KEY (AID)   REFERENCES A (ID)
);

CREATE TABLE C
(
    ID   INT          PRIMARY KEY,
    NAME VARCHAR(32) NULL,
    AID   INT          NULL,
    BID   INT          NULL,
    FOREIGN KEY (AID)  REFERENCES A (ID),
    FOREIGN KEY (BID)  REFERENCES B (ID),
    UNIQUE (BID)
);

CREATE TABLE D
(
    ID   INT          PRIMARY KEY,
    NAME VARCHAR(32) NULL,
    AID   INT          NULL,
    BID   INT          NULL,
    CID   INT          NULL,
    FOREIGN KEY (AID)  REFERENCES A (ID),
    FOREIGN KEY (BID)  REFERENCES B (ID),
    FOREIGN KEY (CID)  REFERENCES C (ID)
);

commit;

quit;
