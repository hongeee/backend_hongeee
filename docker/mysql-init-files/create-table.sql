create table if not exists request_vacation.refresh_token
(
    id            bigint auto_increment
        primary key,
    created_date  datetime(6)  null,
    modified_date datetime(6)  null,
    token         varchar(255) not null,
    token_key     bigint       not null
);

create table if not exists request_vacation.user
(
    id            bigint auto_increment
        primary key,
    created_date  datetime(6)      null,
    modified_date datetime(6)      null,
    annual_days   double default 0 null,
    email         varchar(30)      not null,
    name          varchar(100)     not null,
    password      varchar(100)     null,
    constraint UK_ob8kqyqqgmefl0aco34akdtpe
        unique (email)
);

create table if not exists request_vacation.user_roles
(
    user_id bigint       not null,
    roles   varchar(255) null,
    constraint FK55itppkw3i07do3h7qoclqd4k
        foreign key (user_id) references user (id)
);

create table if not exists request_vacation.vacation
(
    id              bigint auto_increment
        primary key,
    comment         varchar(255) null,
    end_date        date         null,
    period double null,
    start_date      date         null,
    vacation_status varchar(255) null,
    vacation_type   varchar(255) null,
    user_id         bigint       null,
    constraint FK66w7spv7nptavgkgh7m6up8kr
        foreign key (user_id) references user (id)
);