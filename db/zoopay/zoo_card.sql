-- self-operated card
drop table if exists z_card;
create table z_card
(
    id              bigint       not null,
    dept_id         bigint       not null comment '机构ID',
    dept_name       varchar(32)  not null comment '机构名称',
    -- 卡信息
    account_ifsc    varchar(15)  not null default 'NA' comment 'IFSC',
    account_bank    varchar(64)  not null default 'NA' comment '银行名称',
    account_user    varchar(64)  not null comment '账户名称',
    account_no      varchar(32)  not null comment '账号',
    account_upi     varchar(64)  not null default 'NA' comment 'UPI',
    account_info    varchar(512) comment '其他信息',
    bank_balance    decimal(18, 2) comment '银行余额',
    warning_amount  decimal(18, 2) comment '告警金额',
    card_code       varchar(32)  not null default 'nat' comment '卡代码',
    login_id        varchar(32)  not null,
    password        varchar(32)  not null,
    phone           varchar(32),
    init_timeout    int          not null,
    timeout         int          not null,
    gap             int          not null default 30,

    session_timeout int          not null default 100000 comment 'session时间',

    auto_captcha    int not null default 0 comment '',
    auto_sms        int not null default 0 comment '',

    admin_timeout   int          not null default 300 comment '管理等待',
    device_id       varchar(64)  not null default 'NA' comment '设备ID',
    run_config      varchar(256) not null default '{}' comment '运行配置',
    -- basic info
    creator         bigint comment '创建者',
    create_date     datetime comment '创建时间',
    updater         bigint comment '更新者',
    update_date     datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_card';
create index idx_z_card_0 on z_card (dept_id, create_date);
create index idx_z_card_1 on z_card (dept_id);
create unique index uidx_z_card_0 on z_card (dept_id, account_no);

-- 自营卡流水
drop table if exists z_card_log;
create table z_card_log
(
    id           bigint                         not null,
    dept_id      bigint                         not null comment '机构ID',
    dept_name    varchar(32)                    not null comment '机构名称',
    --
    card_id      bigint                         not null,
    card_user    varchar(64) comment '账户名称',
    card_no      varchar(32) comment '账号',
    --
    utr          varchar(32) comment 'utr',
    tn           varchar(6) collate utf8mb4_bin null,
    narration    varchar(128) comment '流水描述',
    amount       bigint                         not null comment '流水金额',
    balance      bigint                         not null comment '余额',
    flag         varchar(8)                     not null comment '记账方向',
    charge_id    bigint comment '收款ID',
    fail_count   int                            not null default 0,
    match_status int                            not null default 0,
    -- basic info
    creator      bigint comment '创建者',
    create_date  datetime comment '创建时间',
    updater      bigint comment '更新者',
    update_date  datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_card_log';
create index idx_z_card_log_0 on z_card_log (dept_id, create_date);
create index idx_z_card_log_1 on z_card_log (dept_id);
create unique index uidx_z_card_log_0 on z_card_log (card_id, narration);
create unique index uidx_z_card_log_1 on z_card_log (charge_id);
