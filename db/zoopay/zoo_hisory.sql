-- 充值流水
drop table if exists z_charge_his;
create table z_charge_his
(
    id                 bigint                         not null,
    dept_id            bigint                         not null comment '机构id',
    dept_name          varchar(32)                    not null comment '机构名称',

    -- 商户请求信息(9)
    merchant_id        bigint                         not null comment '商户id: sys_user.id',
    merchant_name      varchar(32)                    not null comment '商户名称',
    pay_code           varchar(32)                    null comment '支付编码',
    merchant_rate      decimal(4, 4)                  null comment '收款扣率',
    order_id           varchar(64)                    not null comment '商户单号',
    amount             decimal(18, 2)                 not null comment '充值金额',
    real_amount        decimal(18, 2)                 null comment '实际金额',
    merchant_principal decimal(18, 2)                 null comment '商户本金',
    merchant_fee       decimal(18, 2)                 null comment '手续费',
    callback_url       varchar(256)                   not null default 'na' comment '',
    notify_url         varchar(256)                   not null default 'na' comment '',
    ip                 varchar(15)                    null comment '发起充值时ip',

    -- 匹配与凭证(4)
    utr                varchar(32) comment '玩家付款utr: 落地页上传',
    tn                 varchar(6) collate utf8mb4_bin null,
    upi                varchar(32) comment '收款account',
    pictures           varchar(256)                   not null default '[]' comment '凭证',

    -- 处理与状态(6)
    process_status     int                            not null default 0 comment '0: 新建, 1: 处理中, 2: 成功， 3：失败',
    notify_time        datetime comment '通知时间',
    notify_status      int                            not null default 0 comment '通知状态',
    notify_count       int                            not null default 0 comment '通知次数',
    settle_flag        int                            not null default 0 comment '清算标志',
    handle_mode        varchar(16)                    null comment '处理模式: (card, ant, agent)|channel',

    -- 自营卡模式
    card_id            bigint comment '卡id',
    card_user          varchar(32) comment '卡户名',
    card_no            varchar(32) comment '卡账号',

    -- 渠道模式(5)
    channel_label      varchar(32)                    null comment '渠道',
    channel_id         bigint                         null comment '渠道id',
    channel_rate       decimal(4, 4)                  null comment '渠道成本扣率',
    channel_cost       decimal(18, 2)                 null comment '渠道成本',
    channel_order      varchar(64)                    null comment '渠道单号',

    -- 代理跑分模式(9)
    agent_id           bigint comment '代理id',
    agent_name         varchar(32) comment '代理名称',
    agent_rate         decimal(4, 4) comment '',
    agent_share        decimal(18, 2) comment '出借额度',
    user_id            bigint comment '卡主id',
    username           varchar(32) comment '卡主名称',
    user_rate          decimal(4, 4) comment '卡主点位',
    user_card_id       bigint comment '卡主卡id',
    user_card_user     varchar(32) comment '',
    user_card_no       varchar(32) comment '',

    -- 码农跑分模式(9)
    ant_id             bigint comment '码农id',
    ant_name           varchar(32) comment '码农名称',
    ant_card_id        bigint comment '码农卡id',
    ant_card_user      varchar(32) comment '码农卡户名',
    ant_card_no        varchar(32) comment '码农卡号',
    ant_p1_id          bigint,
    ant_p2_id          bigint,
    ant_p1_rate        decimal(4, 4),
    ant_p2_rate        decimal(4, 4),

    -- common
    creator            bigint comment '创建者',
    create_date        datetime comment '创建时间',
    updater            bigint comment '更新者',
    update_date        datetime comment '更新时间',
    primary key (id)
) engine = innodb
  collate utf8mb4_bin
  default character set utf8mb4 comment ='z_charge_his';
create index idx_z_charge_his_0 on z_charge_his (dept_id, create_date, process_status);
create index idx_z_charge_his_1 on z_charge_his (dept_id, merchant_id, create_date, process_status);
create index idx_z_charge_his_2 on z_charge_his (dept_id, channel_id, create_date, process_status);
create index idx_z_charge_his_3 on z_charge_his (dept_id, agent_id, create_date, process_status);
create index idx_z_charge_his_4 on z_charge_his (dept_id, card_id, create_date, process_status);
create index idx_z_charge_his_5 on z_charge_his (dept_id, ant_id, create_date, process_status);

-- 提现流水
drop table if exists z_withdraw_his;
create table z_withdraw_his
(
    id                bigint         not null,
    dept_id           bigint         not null comment '机构id',
    dept_name         varchar(32)    not null comment '机构名称',

    -- 商户请求
    merchant_id       bigint         not null comment '商户ID',
    merchant_name     varchar(32)    not null comment '商户名',
    order_id          varchar(64)    not null comment '商户单号',
    amount            decimal(18, 2) not null comment '订单金额',
    notify_url        varchar(128)   not null default 0 comment '通知地址',
    account_ifsc      varchar(15)    not null comment 'ifsc',
    account_bank      varchar(64)    not null comment '银行名称',
    account_user      varchar(64)    not null comment '账户名称',
    account_no        varchar(32)    not null comment '账号',
    merchant_rate     decimal(4, 4)  not null comment '提现费率',
    merchant_fix      decimal(18, 2) not null comment '提现费率',
    merchant_fee      decimal(18, 2) null comment '提现手续费',
    cost              decimal(18, 2) null comment '提现成本',
    ip                varchar(15)    null comment '提现IP',
    memo              varchar(128),

    -- 凭证与匹配
    utr               varchar(32) comment 'UTR',
    upi               varchar(32)    not null default 'NA' comment 'UPI',
    pictures          varchar(256) comment '凭证',

    -- 状态与处理
    process_status    int            not null default 0 comment '0: 新建,1: 处理中,2: 成功,3:失败',
    notify_status     int            not null default 0 comment '0:未通知, 1:已通知, 2:通知失败',
    notify_count      int            not null default 0 comment '通知次数',
    notify_time       datetime comment '通知时间',
    claimed           int            not null default 0 comment '抢充',
    log_id            bigint comment '卡流水ID, card: z_card_log|ant:z_ant_log|agent:z_user_log',
    handle_mode       varchar(16)    null comment '处理模式',

    -- 渠道模式
    channel_id        bigint         null comment '渠道ID',
    channel_label     varchar(32)    null comment '渠道',
    channel_order     varchar(64)    null comment '渠道单号',
    channel_cost_rate decimal(4, 4)  null comment '提现成本费率',
    channel_cost_fix  decimal(18, 2) null comment '提现成本定额',

    -- 自营卡模式(3)
    card_id           bigint comment '卡ID',
    card_user         varchar(32) comment '卡账户',
    card_no           varchar(32) comment '卡号',

    -- 码农跑分模式(2)
    ant_id            bigint comment '码农ID',
    ant_name          varchar(32) comment '码农',

    -- 代理跑分模式(4)
    agent_id          bigint comment '代理ID',
    agent_name        varchar(32) comment '代理',
    user_id           bigint comment '卡主ID',
    username          varchar(32) comment '卡主',

    -- common
    creator           bigint comment '创建者',
    create_date       datetime comment '创建时间',
    updater           bigint comment '更新者',
    update_date       datetime comment '更新时间',
    primary key (id)
) engine = innodb
  collate utf8mb4_bin
  default character set utf8mb4 comment ='z_withdraw_his';
create index idx_z_withdraw_his_0 on z_withdraw_his (dept_id, create_date, process_status);
create index idx_z_withdraw_his_1 on z_withdraw_his (dept_id, merchant_id, create_date, process_status);
create index idx_z_withdraw_his_2 on z_withdraw_his (dept_id, channel_id, create_date, process_status);
create index idx_z_withdraw_his_3 on z_withdraw_his (dept_id, card_id, create_date, process_status);
create index idx_z_withdraw_his_4 on z_withdraw_his (dept_id, agent_id, create_date, process_status);
create index idx_z_withdraw_his_5 on z_withdraw_his (dept_id, ant_id, create_date, process_status);

-- 流水台账
drop table if exists z_log_his;
create table z_log_his
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
    mutation varchar(64) not null comment '',
    --
    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',
    primary key (id)
) engine = innodb
  collate utf8mb4_bin
  default character set utf8mb4 comment ='z_log_his';
-- attention:  一条凭证， 只能记一次流水, 所以这里是唯一索引
create unique index uidx_z_log_his on z_log_his (dept_id, fact_type, fact_id);
create index idx_z_log_his_0 on z_log_his (dept_id, owner_id, create_date desc);
create index idx_z_log_his_1 on z_log_his (dept_id, owner_id, fact_type, create_date desc);
create index idx_z_log_his_2 on z_log_his (dept_id, owner_type, owner_id, create_date desc);



-- 自营卡流水
drop table if exists z_card_log_his;
create table z_card_log_his
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
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_card_log_his';
create index idx_z_card_log_his_0 on z_card_log_his (dept_id, create_date);
create index idx_z_card_log_his_1 on z_card_log_his (dept_id);
create unique index uidx_z_card_log_his_0 on z_card_log_his (card_id, narration);
create unique index uidx_z_card_log_his_1 on z_card_log_his (charge_id);
