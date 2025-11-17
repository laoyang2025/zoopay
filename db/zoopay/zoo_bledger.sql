-- 机器人账号参数  <-----> sys_user 相对应
drop table if exists bot_account;
create table bot_account
(
    id          bigint        not null comment 'id',
    dept_id     bigint        not null comment '机构ID',
    user_id     bigint        not null comment 'ID',
    user_name   varchar(64)   not null comment '用户名',
    balance     bigint        not null comment '账户余额',
    version     bigint        not null default 0 comment 'version',
    usd_rate    decimal(6, 2) null comment 'USD汇率',
    fee_rate    decimal(3, 3) null comment '手续费率',
    bot_chat    varchar(128)  not null comment '飞机密钥',
    bot_admin   varchar(32)   not null comment '飞机管理员',
    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='机器人账号';
create index idx_bot_account_1 on bot_account (dept_id, create_date);

-- 充值情况
drop table if exists bot_charge;
create table bot_charge
(
    id          bigint              not null comment 'id',
    dept_id     bigint              not null comment '机构ID',
    user_id     bigint              not null comment '用户ID',
    user_name   varchar(64)         not null comment '用户名',
    amount      bigint              not null comment '法币金额',
    fee         bigint              not null comment '手续费',
    fee_rate    decimal(3, 3)       not null comment '手续费率',
    del_flag    tinyint(4) unsigned not null default 0 comment '删除标识  0：未删除    1：删除',
    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='充值';
create index idx_bot_charge_1 on bot_charge (dept_id, create_date);
create index idx_bot_charge_2 on bot_charge (dept_id, user_id, create_date);


-- 代付情况
drop table if exists bot_pay;
create table bot_pay
(
    id          bigint              not null comment 'id',
    dept_id     bigint              not null comment '机构ID',
    user_id     bigint              not null comment 'ID',
    user_name   varchar(64)         not null comment '用户名',
    amount  bigint              not null comment 'USD金额',
    del_flag    tinyint(4) unsigned not null default 0 comment '删除标识  0：未删除    1：删除',
    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='付款';
create index idx_bot_pay_1 on bot_pay (dept_id, create_date);
create index idx_bot_pay_2 on bot_pay (dept_id, user_id, create_date);

-- 余额流水
drop table if exists bot_log;
create table bot_log
(
    id          bigint       not null comment 'id',
    dept_id     bigint       not null comment '机构ID',
    user_id     bigint       not null comment 'ID',
    user_name   varchar(64)  not null comment '用户名',

    old_amount  bigint       not null comment '旧余额',
    new_amount  bigint       not null comment '新余额',
    amount      bigint       not null comment '发生额',
    fact_id     bigint       not null comment '事实id',
    fact_type   int          not null comment '事实类型',
    fact_memo   varchar(128) not null comment '事实简介',
    version     bigint       not null default 0,

    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='余额流水';
create index idx_bot_log_1 on bot_log (dept_id, create_date);
create index idx_bot_log_2 on bot_log (dept_id, user_id, create_date);
