package io.renren.bledger.dao;

import io.renren.bledger.entity.BotLogEntity;
import io.renren.commons.mybatis.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
* 余额流水
*
* @author kernel kernel@qq.com
* @since 1.0 2024-06-02
*/
@Mapper
public interface BotLogDao extends BaseDao<BotLogEntity> {
	
}