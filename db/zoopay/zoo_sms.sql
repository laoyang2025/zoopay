-- 短信内容
drop table if exists z_sms;
create table z_sms
(
    id           bigint         not null,
    dept_id      bigint         not null comment '机构id',
    dept_name    varchar(32)    not null comment '机构名称',

    content      varchar(512)   null,
    phone        varchar(32)    not null,
    device_id    varchar(64)    null,
    card_id      bigint,
    utr          varchar(16)    null,
    amount       decimal(18, 2) null comment '金额',
    match_status int            not null default 0,
    fail_count   int            not null default 0,
    charge_id    bigint,
    md5          varchar(64)    not null,

    -- common
    creator      bigint comment '创建者',
    create_date  datetime comment '创建时间',
    updater      bigint comment '更新者',
    update_date  datetime comment '更新时间',
    primary key (id)
) engine = innodb
  collate utf8mb4_bin
  default character set utf8mb4 comment ='z_sms';
create unique index uidx_z_sms_0 on z_sms (md5);
create index idx_z_sms_0 on z_sms (utr);
