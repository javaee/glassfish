Drop table TXLEVELSWITCH;

CREATE TABLE TXLEVELSWITCH (
    c_id            integer not null,
    c_phone         char(16)
);

INSERT INTO TXLEVELSWITCH VALUES(10, 'abcd');
INSERT INTO TXLEVELSWITCH VALUES(11, 'pqrs');

Drop table TXLEVELSWITCH2;

CREATE TABLE TXLEVELSWITCH2 (
    c_id            integer not null,
    c_phone         char(16)
);

INSERT INTO TXLEVELSWITCH2 VALUES(1, 'abcd');
INSERT INTO TXLEVELSWITCH2 VALUES(2, 'pqrs');

