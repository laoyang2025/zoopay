package io.renren.bledger.dao;

import io.renren.bledger.entity.BotAccountEntity;
import io.renren.commons.mybatis.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
* 机器人账号
*
* @author kernel kernel@qq.com
* @since 1.0 2024-06-02
*/
@Mapper
public interface BotAccountDao extends BaseDao<BotAccountEntity> {
	
}