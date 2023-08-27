CREATE DATABASE myapp;
use myapp;

create table member (
`name` varchar(255) NOT NULL,
`phone` bigint NOT NULL,
`email` varchar(255) NOT NULL,
`password` varchar(255) NOT NULL,
primary key (name)
) engine=InnoDB;

create table financial_history (
`date` varchar(255) NOT NULL,
`deposit` bigint not null,
`withdraw` bigint not null,
`balance` bigint not null,
primary key (date)
) engine=InnoDB;
/*
member와 financial_history의 테이블을 만들었음.
*/
select * from auth_member;
select * from auth_financial_history;