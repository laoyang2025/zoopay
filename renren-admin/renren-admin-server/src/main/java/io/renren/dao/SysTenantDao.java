/**
 * Copyright (c) 2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.dao;

import io.renren.commons.mybatis.dao.BaseDao;
import io.renren.entity.SysTenantEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 租户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface SysTenantDao extends BaseDao<SysTenantEntity> {

    List<SysTenantEntity> getList(Map<String, Object> params);

    SysTenantEntity getById(Long id);

    void deleteBatch(Long[] ids);
}