-- 余额表
drop table if exists z_balance;
create table z_balance
(
    id          bigint         not null,                  -- 默认id就是属主ID, 如果主体有多个账户, 则不为属主
    dept_id     bigint         not null comment '机构id',
    dept_name   varchar(32)    not null comment '机构名称',

    owner_type  varchar(16)    not null comment '账户类型',
    owner_id    bigint         not null comment '账户id', -- owner id
    owner_name  varchar(32)    not null comment '账户名称',

    balance     decimal(18, 2) not null default 0,
    version     bigint         not null default 0,

    -- common
    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',
    primary key (id)
) engine = innodb
  collate utf8mb4_bin
  default character set utf8mb4 comment ='z_balance';
create index idx_z_balance_1 on z_balance (dept_id, owner_type);
create index idx_z_balance_2 on z_balance (dept_id, owner_type, owner_name);
create index uidx_z_balance_0 on z_balance (dept_id, owner_type, owner_id);


-- 流水台账
drop table if exists z_log;
create table z_log
(
    id          bigint         not null,
    dept_id     bigint         not null comment '机构id',
    dept_name   varchar(32)    not null comment '机构名称',
    -- merchant / agent / user / ant / basket / channel / card
    balance_id  bigint         not null comment '余额ID',
    owner_type  varchar(16)    not null comment '账户类型',
    owner_id    bigint         not null comment '账户id',
    owner_name  varchar(32)    not null comment '账户名称',
    fact_id     bigint         not null comment '事实id',
    fact_type   int            not null comment '事实类型',
    fact_amount decimal(18, 2) not null comment '金额: 为正代表入金， 为负代表出金',
    fact_memo   varchar(128)   not null comment '事实说明',
    old_balance decimal(18, 2) not null comment '旧余额',
    new_balance decimal(18, 2) not null comment '新余额',
    mutation    varchar(64)    not null comment '版本变化',
    --
    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',
    primary key (id)
) engine = innodb
  collate utf8mb4_bin
  default character set utf8mb4 comment ='z_log';
-- attention:  一条凭证， 只能记一次流水, 所以这里是唯一索引
create unique index uidx_z_log on z_log (dept_id, fact_type, fact_id);
create index idx_z_log_0 on z_log (dept_id, owner_id, create_date desc);
create index idx_z_log_1 on z_log (dept_id, owner_id, fact_type, create_date desc);
create index idx_z_log_2 on z_log (dept_id, owner_type, owner_id, create_date desc);

