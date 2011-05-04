drop table PRODUCT
go

create table PRODUCT
(
  DESCRIPTION VARCHAR(255),
  PRICE DOUBLE PRECISION not null,
  PRODUCTID VARCHAR(255) not null constraint PK_PRODUCT primary key
)
go
