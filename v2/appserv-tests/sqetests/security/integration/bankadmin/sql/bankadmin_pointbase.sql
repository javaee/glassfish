// Description: SQL schema for cmp beans of bank admin application in security module test
// author: $author$ jagadesh.munta@sun.com
// date: $date$ 01/28/2003
//#######################################################################


// Description: SQL schema for cmp beans of bank admin application in security module test
// author: $ author $ jagadesh.munta@sun.com
// date: $date$ 01/28/2003
//#######################################################################

CREATE TABLE sec_bankadmin_customerejb (customerid VARCHAR2(255) PRIMARY KEY, customername VARCHAR2(255));

CREATE TABLE  sec_bankadmin_accountejb (accountid VARCHAR2(255) PRIMARY KEY, amount NUMERIC, prv BLOB(5000),fk_customerejb varchar2(255)); 

ALTER TABLE sec_bankadmin_accountejb ADD  CONSTRAINT fk_customerejb  FOREIGN KEY (fk_customerejb) REFERENCES sec_bankadmin_customerejb(customerid) ON DELETE CASCADE;


