drop table product;

create table product
(
  description VARCHAR(255),
  price double precision not null,
  productid VARCHAR(255) constraint pk_product primary key NOT NULL
);
