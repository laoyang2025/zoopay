-- 运营收款账户
drop table if exists z_basket;
create table z_basket
(
    id           bigint      not null,
    dept_id      bigint      not null comment '机构id',
    dept_name    varchar(32) not null comment '机构名称',
    enabled      int         not null default 1 comment '启用|禁用',
    -- info
    account_user varchar(32) not null comment '公户户名',
    account_no   varchar(32) not null comment '公户账号',
    account_bank varchar(32) not null comment '公户银行',
    account_ifsc varchar(32) not null comment 'ifsc',
    -- common
    creator      bigint comment '创建者',
    create_date  datetime comment '创建时间',
    updater      bigint comment '更新者',
    update_date  datetime comment '更新时间',
    primary key (id)
) engine = innodb
  collate utf8mb4_bin
  default character set utf8mb4 comment ='z_basket';
create index idx_z_basket_0 on z_basket (dept_id, create_date);

--  路由
drop table if exists z_route;
create table z_route
(
    id        bigint      not null,
    dept_id   bigint      not null comment '机构id',
    dept_name varchar(32) not null comment '机构名称',
    merchant_id bigint not null comment '商户ID',
    merchant_name varchar(32) not null comment '商户名',
    enabled int not null default 0 comment '启用',

    route_type  varchar(32) not null comment 'charge | withdraw',
    process_mode varchar(16) not null comment '路由模式',
    object_id bigint not null comment '目标ID',
    object_name varchar(128) not null comment '目标名称',
    weight int  null comment '收款权重',

    -- 收款路由支付编码路由
    pay_code varchar(32)  null comment '支付编码',
    charge_rate decimal(4,4) null comment '收款扣率',
    big_amount decimal(10,2) null comment '金额路由',

     -- common
    creator        bigint comment '创建者',
    create_date    datetime comment '创建时间',
    updater        bigint comment '更新者',
    update_date    datetime comment '更新时间',
    primary key (id)
) engine = innodb
  collate utf8mb4_bin
  default character set utf8mb4 comment ='z_route';
create index idx_z_route_0 on z_route (dept_id, create_date);
create unique index uidx_z_route_0 on z_route (merchant_id, route_type, pay_code, object_id);

--  路由
drop table if exists z_route;
create table z_route
(
    id        bigint      not null,
    dept_id   bigint      not null comment '机构id',
    dept_name varchar(32) not null comment '机构名称',
    enabled  int not null default 1,
    merchant_id bigint not null comment '商户ID',
    charge_enabled int not null default 0 comment '收款啟用',
    withdraw_enabled int not null default 0 comment '代付用',
    merchant_name varchar(32) not null comment '商户名',
    process_mode varchar(16) not null comment '路由模式',
    -- 支付编码路由
    pay_code varchar(32)  null comment '支付编码',
    charge_rate decimal(4,4) null comment '收款扣率',
    -- 金额路由
    big_amount decimal(10,2) null comment '金额路由',
    object_id bigint not null comment '目标ID',
    object_name varchar(128) not null comment '目标名称',
    weight int not null comment '收款权重',
    withdraw_weight int null comment '付款权重',
    -- common
    creator        bigint comment '创建者',
    create_date    datetime comment '创建时间',
    updater        bigint comment '更新者',
    update_date    datetime comment '更新时间',
    primary key (id)
) engine = innodb
  collate utf8mb4_bin
  default character set utf8mb4 comment ='z_route';
create index idx_z_route_0 on z_route (dept_id, create_date);
create unique index uidx_z_route_0 on z_route (dept_id, merchant_id, object_id);

-- 客服机器人
drop table if exists z_bot;
create table z_bot
(
    id          bigint        not null comment 'id',
    dept_id     bigint        not null comment '机构ID',
    chat_id     varchar(32)   not null comment '',
    serve_id    bigint       not null comment 'merchantId: 服务于哪个商户,   cardId: 服务于哪个渠道',
    serve_name  varchar(32) not null comment '商户名称渠道名称',
    serve_type  varchar(32)  not null comment 'payment: 作为平台服务与商户, merchant: 作为商户服务于渠道',
    creator     bigint comment '创建者',
    create_date datetime comment '创建时间',
    updater     bigint comment '更新者',
    update_date datetime comment '更新时间',
    primary key (id)
) ENGINE = InnoDB
  collate utf8mb4_bin
  DEFAULT CHARACTER SET utf8mb4 COMMENT ='机器人账号';
create index idx_z_bot_1 on z_bot (dept_id, create_date);