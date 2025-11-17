-- ---------------------------------------------------------------------------------
-- 卡+商户 每日
drop view if exists vd_charge_card;
create view vd_charge_card as
select
    sum(if(process_status = 2, real_amount, 0)) as success_amount,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,
    date(create_date) as create_date,
    merchant_name,
    card_no,
    card_user,
    dept_id,
    dept_name
from z_charge
where handle_mode = 'card' and card_id is not null and create_date > now() - interval 35 day
group by dept_id, dept_name, date(create_date), merchant_name, card_no, card_user
order by create_date desc;

-- ---------------------------------------------------------------------------------
-- 渠道+商户 每日
drop view if exists vd_charge_channel;
create view vd_charge_channel as
select
    sum(if(process_status = 2, real_amount, 0)) as success_amount,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,
    date(create_date) as create_date,
    merchant_name,
    channel_label,
    dept_name,
    dept_id
from z_charge
where handle_mode = 'channel' and create_date > now() - interval 35 day
group by dept_id, dept_name, date(create_date), merchant_name, channel_label
order by create_date desc;

-- ---------------------------------------------------------------------------------
-- 卡+商户 小时数据
drop view if exists vh_charge_card;
create view vh_charge_card as
select
    sum(if(process_status = 2, real_amount, 0)) as success_amount,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,

    date_format(create_date, '%Y-%m-%d %h:00:00') as create_date,
    merchant_name,
    card_no,
    card_user,
    dept_name,
    dept_id
from z_charge
where handle_mode = 'card' and create_date > NOW() - INTERVAL 48 HOUR
group by dept_id, dept_name,  date_format(create_date, '%Y-%m-%d %h:00:00'), merchant_name, card_no, card_user
order by create_date desc;
-- ---------------------------------------------------------------------------------
-- 卡 小时数据
drop view if exists vh_card;
create view vh_card as
select
    sum(if(process_status = 2, real_amount, 0)) as success_amount,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,
    date_format(create_date, '%Y-%m-%d %h:00:00') as create_date,
    card_no,
    card_user,
    dept_name,
    dept_id
from z_charge
where handle_mode = 'card' and create_date > NOW() - INTERVAL 48 HOUR
group by dept_id, dept_name,  date_format(create_date, '%Y-%m-%d %h:00:00'), card_no, card_user
order by create_date desc;

-- ---------------------------------------------------------------------------------
-- 商户 小时数据
drop view if exists vh_merchant;
create view vh_merchant as
select
    sum(if(process_status = 2, real_amount, 0)) as success_amount,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,
    date_format(create_date, '%Y-%m-%d %h:00:00') as create_date,
    merchant_name,
    dept_name,
    dept_id
from z_charge
where create_date > NOW() - INTERVAL 48 HOUR
group by dept_id, dept_name,  date_format(create_date, '%Y-%m-%d %h:00:00'), merchant_name
order by create_date desc;

-- ---------------------------------------------------------------------------------
-- 卡 整日
drop view if exists vd_card;
create view vd_card as
select
    sum(if(process_status = 2, real_amount, 0)) as success_amount,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,
    date(create_date) as create_date,
    card_no,
    card_user,
    dept_name,
    dept_id
from z_charge
where handle_mode = 'card' and card_id is not null and create_date > now() - interval 35 day
group by dept_id, dept_name, date(create_date), card_no, card_user
order by create_date desc;

-- 渠道 整日
drop view if exists vd_channel;
create view vd_channel as
select
    sum(if(process_status = 2, real_amount, 0)) as success_amount,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,
    --
    date(create_date) as create_date,
    channel_label,
    dept_name,
    dept_id
from z_charge
where handle_mode = 'channel' and create_date > now() - interval 35 day
group by dept_id, dept_name, date(create_date), channel_label
order by create_date desc;

-- ---------------------------------------------------------------------------------
-- 商户 整日
drop view if exists vd_merchant;
create view vd_merchant as
select
    sum(if(process_status = 2, real_amount, 0)) as success_amount,
    sum(if(process_status = 2, merchant_fee, 0)) as fee,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,
    date(create_date) as create_date,
    merchant_name,
    dept_name,
    dept_id
from z_charge
where create_date > now() - interval 35 day
group by dept_id, dept_name, date(create_date), merchant_name
order by create_date desc;

-- ---------------------------------------------------------------------------------
-- 商户 整日
drop view if exists vd_merchant_withdraw;
create view vd_merchant_withdraw as
select
    sum(if(process_status = 2, amount, 0)) as success_amount,
    sum(if(process_status = 2, merchant_fee, 0)) as fee,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,
    date(create_date) as create_date,
    merchant_name,
    dept_name,
    dept_id
from z_withdraw
where create_date > now() - interval 35 day
group by dept_id, dept_name, date(create_date), merchant_name
order by create_date desc;

-- ---------------------------------------------------------------------------------
-- 机构每日收款情况
drop view if exists vd_dept_charge;
create view vd_dept_charge as
select
    sum(if(process_status = 2, real_amount, 0)) as success_amount,
    sum(if(process_status = 2, merchant_fee, 0)) as merchant_fee,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status <> 2, 1, 0)) as fail,
    sum(if(process_status = 2, 1, 0))  / (sum(if(process_status = 2, 1, 0)) + sum(if(process_status <> 2, 1, 0))) as success_rate,
    sum(if(process_status = 2, channel_cost, 0)) as channel_cost,
    date(create_date) as create_date,
    dept_name,
    dept_id
from z_charge
where create_date > now() - interval  93 day
group by dept_id, dept_name, date(create_date)
order by create_date desc;

-- ---------------------------------------------------------------------------------
-- 机构每日代付情况
drop view if exists vd_dept_withdraw;
create view vd_dept_withdraw as
select
    sum(if(process_status = 2, amount, 0)) as success_amount,
    sum(if(process_status = 2, merchant_fee, 0)) as merchant_fee,
    sum(if(process_status = 2, cost, 0)) as cost,
    sum(if(process_status = 2, 1, 0)) as success,
    sum(if(process_status = 2, 0, 1)) as fail,
    date(create_date) as create_date,
    dept_name,
    dept_id
from z_withdraw
where create_date > now() - interval  93 day
group by dept_id, dept_name, date(create_date)
order by create_date desc;
