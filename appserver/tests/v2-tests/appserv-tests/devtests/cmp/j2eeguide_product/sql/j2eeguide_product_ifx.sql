drop table product;

create table product
(
  description VARCHAR(127),
  price double precision not null,
  productid VARCHAR(127) primary key
);

commit;

exit;
