drop table DELIVERYSERVICE_CUSTOMER;
drop table DELIVERYSERVICE;
drop table "ORDER";
drop table CUSTOMER;
drop table ADDRESS;

create table ADDRESS (
        ID              bigint          primary key not null,
        STREET          varchar(255)    not null,
        VERSION         bigint
);

create table CUSTOMER (
        ID              bigint          primary key not null,
        DESCRIPTION     varchar(255)    not null,
        ADDRESS_ID      bigint,
        foreign key (ADDRESS_ID)        references ADDRESS(ID),
        VERSION         bigint
);

create table "ORDER" (
       ID               bigint primary key not null,
       ITEMID           bigint not null,
       QUANTITY         int    not null,
       CUSTOMER_ID      bigint,
       foreign key  (CUSTOMER_ID)   references CUSTOMER(ID),
       VERSION          bigint
);

create table DELIVERYSERVICE (
        SERVICENAME     varchar(255)    primary key not null,
        PRICECATEGORY   int             not null
);

create table DELIVERYSERVICE_CUSTOMER (
        SERVICEOPTIONS_SERVICENAME  varchar(255),
        foreign key (SERVICEOPTIONS_SERVICENAME)
                references DELIVERYSERVICE(SERVICENAME),

        CUSTOMERS_ID                bigint,
        foreign key (CUSTOMERS_ID)
                references CUSTOMER(ID)
);

