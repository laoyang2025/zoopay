


-- 卡主的卡
drop table if exists z_user_card;
create table z_user_card
(
    id           bigint       not null,
    dept_id      bigint       not null comment '机构ID',
    dept_name    varchar(32)  not null comment '机构名称',
    --
    agent_id     bigint       not null comment '代理ID',
    agent_name   varchar(21)  not null comment '代理名称',
    user_id      bigint       not null comment '卡主ID',
    username     varchar(32)  not null comment '卡主名称',
    enabled      int          not null default 1 comment '启用状态',
    -- 绑定银行卡信息
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

--     init_timeout   int          not null,
--     timeout        int          not null,
--     gap       int          not null default 30,
--     hold           int          not null default 0,

    run_config   varchar(256) not null default '{}' comment '运行配置',
    -- basic info
    creator      bigint comment '创建者',
    create_date  datetime comment '创建时间',
    updater      bigint comment '更新者',
    update_date  datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_user_card';
create index idx_z_user_card_0 on z_user_card (dept_id, agent_id, user_id, create_date);

-- 代理充值
drop table if exists z_agent_charge;
create table z_agent_charge
(
    id             bigint       not null,
    dept_id        bigint       not null comment '机构ID',
    dept_name      varchar(32)  not null comment '机构名称',
    agent_id       bigint       not null comment '代理ID',
    agent_name     varchar(32)  not null comment '代理名称',
    --
    amount         bigint       not null comment '充值金额',
    basket_id      bigint       not null comment '对公户ID',
    account_user   varchar(32)  not null comment '账号名称',
    account_no     varchar(32)  not null comment '账号',
    account_bank   varchar(32)  not null comment '银行名称',
    account_ifsc   varchar(32)  not null comment 'IFSC',
    --
    process_status int          not null comment '状态',
    utr            varchar(32)  not null,
    pictures       varchar(256) not null default '[]',
    -- common
    creator        bigint comment '创建者',
    create_date    datetime comment '创建时间',
    updater        bigint comment '更新者',
    update_date    datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_agent_charge';
create index idx_z_agent_charge_0 on z_agent_charge (dept_id, agent_id, create_date);

-- 代理提现
drop table if exists z_agent_withdraw;
create table z_agent_withdraw
(
    id             bigint       not null,
    dept_id        bigint       not null comment '机构ID',
    dept_name      varchar(32)  not null comment '机构名称',
    agent_id       bigint       not null,
    agent_name     varchar(32)  not null,
    --
    basket_id      bigint comment '对公户ID',
    basket_user    bigint comment '对公户名称',
    basket_no      bigint comment '对公户账号',
    --
    amount         bigint       not null comment '提现金额',
    account_user   varchar(32)  not null comment '提现账户名',
    account_no     varchar(32)  not null comment '提现账号',
    account_bank   varchar(32)  not null comment '提现银行',
    account_ifsc   varchar(32)  not null comment '提现ifsc',
    --
    process_status int          not null default 0,
    utr            varchar(32)  not null comment 'utr',
    pictures       varchar(256) not null default '[]' comment '图片',
    -- common
    creator        bigint comment '创建者',
    create_date    datetime comment '创建时间',
    updater        bigint comment '更新者',
    update_date    datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_agent_withdraw';
create index idx_z_agent_withdraw_0 on z_agent_withdraw (dept_id, agent_id, create_date);

-- 卡主充值
drop table if exists z_user_charge;
create table z_user_charge
(
    id             bigint        not null,
    dept_id        bigint        not null comment '机构ID',
    dept_name      varchar(32)   not null comment '机构名称',
    --
    agent_id       bigint        not null comment '代理ID',
    agent_name     varchar(21)   not null comment '代理名称',
    user_id        bigint        not null comment '卡主ID',
    username       varchar(32)   not null comment '卡主名称',
    --
    amount         bigint        not null comment '充值金额',
    charge_rate    decimal(4, 4) not null comment '充值点位',
    fee            bigint        not null comment '收入',
    assign_type    varchar(32)   not null comment '分配类型: basket | withdraw',
    basket_id      bigint comment 'not null if assign_type == basket',
    withdraw_id    bigint comment 'not null if assign_type == withdraw',
    --
    account_user   varchar(32)   not null comment '账户名',
    account_no     varchar(32)   not null comment '账号',
    account_bank   varchar(32)   not null comment '银行',
    account_ifsc   varchar(32)   not null comment 'ifsc',
    --
    process_status int           not null comment '状态',
    utr            varchar(32)   not null comment 'utr',
    pictures       varchar(256)  not null default '[]' comment '凭证',
    -- common
    creator        bigint comment '创建者',
    create_date    datetime comment '创建时间',
    updater        bigint comment '更新者',
    update_date    datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_user_charge';
create index idx_z_user_charge_0 on z_user_charge (dept_id, agent_id, user_id, create_date);

-- 卡主提现
drop table if exists z_user_withdraw;
create table z_user_withdraw
(
    id             bigint        not null,
    dept_id        bigint        not null comment '机构ID',
    dept_name      varchar(32)   not null comment '机构名称',
    --
    agent_id       bigint        not null comment '代理ID',
    agent_name     varchar(32)   not null comment '代理名称',
    user_id        bigint        not null comment '卡主ID',
    username       bigint        not null comment '卡主名称',
    --
    basket_id      bigint        not null comment '公户ID',
    basket_user    bigint        not null comment '公户名',
    basket_no      bigint        not null comment '公户账号',
    --
    amount         bigint        not null comment '提现金额',
    out_rate       decimal(4, 4) not null comment '提现费率',
    out_fee        bigint        not null comment '提现费用',
    --
    account_user   varchar(32)   not null comment '账户名',
    account_no     varchar(32)   not null comment '账号',
    account_bank   varchar(32)   not null comment '银行名称',
    account_ifsc   varchar(32)   not null comment 'ifsc',
    --
    process_status int           not null default 0 comment '状态',
    utr            varchar(32)   not null comment 'UTR',
    pictures       varchar(256)  not null default '[]' comment '凭证',
    -- common
    creator        bigint comment '创建者',
    create_date    datetime comment '创建时间',
    updater        bigint comment '更新者',
    update_date    datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_user_withdraw';
create index idx_z_user_withdraw_0 on z_user_withdraw (dept_id, create_date);
create index idx_z_user_withdraw_1 on z_user_withdraw (dept_id, agent_id, create_date);

-- 卡主银行卡流水
drop table if exists z_user_log;
create table z_user_log
(
    id          bigint                          not null,
    dept_id     bigint                          not null comment '机构ID',
    dept_name   varchar(32)                     not null comment '机构名称',
    --
    agent_id    bigint,
    agent_name  varchar(32),
    user_id     bigint,
    username    varchar(32),
    --
    card_id     bigint,
    card_user   varchar(32),
    card_no     varchar(32),
    --
    utr         varchar(32) comment 'utr',
    tn          varchar(32) collate utf8mb4_bin null,
    narration   varchar(128) comment '流水描述',
    amount      bigint                          not null comment '流水金额',
    balance     bigint                          not null comment '余额',
    flag        varchar(8)                      not null comment '记账方向',
    charge_id   bigint comment '收款ID',
    fail_count  int                             not null default 0,
    match_status  int                            not null default 0,

    -- basic info
    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',

    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_user_log';
create index idx_z_user_log_0 on z_user_log (dept_id, create_date);
create index idx_z_user_log_1 on z_user_log (dept_id, username);
create index idx_z_user_log_2 on z_user_log (dept_id, card_user);
create index idx_z_user_log_3 on z_user_log (dept_id, agent_id, username);
create index idx_z_user_log_4 on z_user_log (dept_id, agent_id, card_user);
create unique index uidx_z_user_log_0 on z_user_log (charge_id);
create unique index uidx_z_user_log_1 on z_user_log(card_id, narration)

