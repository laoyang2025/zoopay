

-- 码农卡信息
drop table if exists z_ant_card;
create table z_ant_card
(
    id           bigint       not null,
    dept_id      bigint       not null comment '机构ID',
    dept_name    varchar(32)  not null comment '机构名称',
    ant_id      bigint       not null,
    ant_name     varchar(32)  not null,
    -- 银行卡信息
    account_ifsc varchar(15)  not null default 'NA' comment 'IFSC',
    account_bank varchar(64)  not null default 'NA' comment '银行名称',
    account_user varchar(64)  not null comment '账户名称',
    account_no   varchar(32)  not null comment '账号',
    account_upi  varchar(32)  not null default 'NA',
    account_info  varchar(32)  not null default 'NA',
    bank_balance decimal(18,2),
    card_code    varchar(32) not null default 'nat' comment '卡代码',
    login_id       varchar(32)  not null,
    password       varchar(32)  not null,
    phone          varchar(32),
    init_timeout   int          not null,
    timeout        int          not null,
    gap       int          not null default 30,
    hold           int          not null default 0,

    run_config   varchar(256) not null default '{}' comment '运行配置',
    -- basic info
    creator      bigint comment '创建者',
    create_date  datetime comment '创建时间',
    updater      bigint comment '更新者',
    update_date  datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_ant_card';
create index idx_z_ant_card_0 on z_ant_card (dept_id, create_date);
create unique index uidx_z_ant_card_0 on z_ant_card (dept_id, ant_id, account_no);

-- 码农充值流水
drop table if exists z_ant_charge;
create table z_ant_charge
(
    id             bigint        not null,
    dept_id        bigint        not null comment '机构ID',
    dept_name      varchar(32)   not null comment '机构名称',
    ant_id        bigint        not null comment '',
    ant_name      bigint        not null comment '',
    amount         decimal(4, 4) not null comment '充值金额',
    -- 分配给公户出款, 商户代付
    assign_type    varchar(16) comment 'basket | withdraw',
    basket_id      bigint comment '对公账户id',
    withdraw_id    bigint comment '提现ID',
    --
    utr            varchar(32) comment 'UTR',
    pictures       varchar(256)  not null default '[]' comment '凭证',
    --
    process_status int           not null default 0 comment '处理状态',
    settle_flag    int           not null default 0 comment '清算标志',
    -- common
    creator        bigint comment '创建者',
    create_date    datetime comment '创建时间',
    updater        bigint comment '更新者',
    update_date    datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_charge';
create index idx_z_ant_charge_0 on z_ant_charge (dept_id, create_date, process_status);
create index idx_z_ant_charge_1 on z_ant_charge (dept_id, ant_id, process_status, create_date);
create index idx_z_ant_charge_2 on z_ant_charge (dept_id, ant_id, create_date, process_status);

-- 码农银行卡流水
drop table if exists z_ant_log;
create table z_ant_log
(
    id          bigint                          not null,
    dept_id     bigint                          not null comment '机构ID',
    dept_name   varchar(32)                     not null comment '机构名称',
    --
    ant_id      bigint                          not null comment '码农ID',
    ant_name    varchar(32)                     not null comment '码农名称',
    --
    card_id     bigint                          not null comment '卡ID',
    card_user   varchar(32)                     not null comment '卡户名',
    card_no     varchar(32)                     not null comment '卡号',
    --
    utr         varchar(32) comment 'UTR',
    narration   varchar(128) comment '流水描述',
    amount      bigint                          not null comment '流水金额',
    balance     bigint                          not null comment '余额',
    flag        varchar(8)                      not null comment '记账方向',
    tn          varchar(32) collate utf8mb4_bin null,
    charge_id   bigint comment '匹配收款ID',
    fail_count  int                             not null default 0 comment '失败次数',
    match_status  int                            not null default 0,

    -- basic info
    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_ant_log';
create index idx_z_ant_log_0 on z_ant_log (dept_id, create_date);
create index idx_z_ant_log_1 on z_ant_log (dept_id, ant_name, create_date);
create unique index uidx_z_ant_log_0 on z_ant_log (charge_id);
create unique index uidx_z_ant_log_1 on z_ant_log (card_id, narration);
