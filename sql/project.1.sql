use myapp;

create table auth_member(
id bigint not null auto_increment, 
balance bigint not null, 
date date not null, 
deposit bigint not null, 
name varchar(255) not null, 
secret varchar(500), 
withdraw bigint not null, 
primary key (id)
) engine=InnoDB;

create table auth_financial_history (
id bigint not null auto_increment, 
balance bigint not null, 
date date, 
deposit bigint not null, 
withdraw bigint not null, 
member_id bigint, 
primary key (id)
) engine=InnoDB;

/*
alter table 
auth_financial_history add 
constraint 
FKhyefggi6lrxf07g6q2ghv79d0 
foreign key (member_id) 
references auth_member (id)
*/

select * from auth_member;
select * from auth_financial_history;