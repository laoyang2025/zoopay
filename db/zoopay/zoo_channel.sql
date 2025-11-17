-- 支付渠道
drop table if exists z_channel;
create table z_channel
(
    id                 bigint        not null,
    dept_id            bigint        not null comment '机构ID',
    dept_name          varchar(32)   not null comment '机构名称',
    -- 渠道控制
    charge_enabled     int           not null default 0 comment '收款启用',
    withdraw_enabled   int           not null default 0 comment '代付启用',
    -- 渠道信息
    channel_name       varchar(32)   not null comment '渠道名称: 技术接入用: lib/xxxx.jar  io.renren.zoo.channel.ChannnelName',
    channel_label      varchar(64)   not null comment '界面展示名称',
    -- fee rate
    charge_rate        decimal(4, 4) not null default 0 comment '充值扣率',
    withdraw_rate      decimal(4, 4) not null default 0 comment '提现扣率',
    withdraw_fix       decimal(18,2) not null default 0 comment '提现定额',
    -- config
    merchant_id        varchar(32)   not null comment '接入商户号',
    pay_code           varchar(32)   not null default 'NA' comment '支付通道编码',
    balance_memo       varchar(128)  null comment '余额详情',
    balance            decimal(18,2)  null comment '实际余额',
    warning_amount     decimal(18,2)  not null default 10000000000.00,
    -- 渠道接口
    charge_url        varchar(128)  not null default '' comment '收款接口地址',
    withdraw_url       varchar(128)  not null default '' comment '代付接口地址',
    charge_query_url  varchar(128)  not null default '' comment '收款查询接口地址',
    withdraw_query_url varchar(128)  not null default '' comment '代付查询接口',
    balance_url        varchar(128)  not null default '' comment '余额查询接口地址',
    ext1               varchar(128),
    ext2               varchar(128),
    ext3               varchar(128),
    -- 密钥安全
    public_key         varchar(2048) null comment '公钥',
    private_key        varchar(2048) null comment '私钥 Or md5密钥',
    platform_key       varchar(2048) null comment '平台公钥',
    white_ip           varchar(128)  not null default '127.0.0.1' comment '白名单',
    -- common
    creator            bigint comment '创建者',
    create_date        datetime comment '创建时间',
    updater            bigint comment '更新者',
    update_date        datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='z_channel';
create index idx_z_channel_0 on z_channel (dept_id, channel_name);
