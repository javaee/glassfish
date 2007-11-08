drop table DELIVERYSERVICE_CUSTOMER cascade constraints;
drop table DELIVERYSERVICE cascade constraints;
drop table "ORDER" cascade constraints;
drop table CUSTOMER cascade constraints;
drop table ADDRESS cascade constraints;

create table ADDRESS (
        ID              number(10)      primary key,
        STREET          varchar2(255)   not null,
        VERSION         number(10)      null
);

create table CUSTOMER (
        ID              number(10)      primary key,
        DESCRIPTION     varchar2(255)   not null,
        ADDRESS_ID      number(10)      references ADDRESS(ID) not null,
        VERSION         number(10)      null
);

create table "ORDER" (
       ID               number(10)    primary key,
       ITEMID           number(10)    not null,
       QUANTITY         number(10)    not null,
       CUSTOMER_ID      number(10)    references CUSTOMER(ID) not null,
       VERSION          number(10)    null
);

create table DELIVERYSERVICE (
        SERVICENAME     varchar2(255)   primary key,
        PRICECATEGORY   number(10)      not null
);

create table DELIVERYSERVICE_CUSTOMER (
        SERVICEOPTIONS_SERVICENAME
                varchar2(255)   references DELIVERYSERVICE(SERVICENAME) not null,
        CUSTOMERS_ID
                number(10)      references CUSTOMER(ID) not null
);

