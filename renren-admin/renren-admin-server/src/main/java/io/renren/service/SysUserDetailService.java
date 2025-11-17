package io.renren.service;

import io.renren.commons.security.user.MyUserDetail;

/**
 * UserDetail Service
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface SysUserDetailService {
    /**
     * 根据用户ID，获取用户详情
     */
    MyUserDetail getUserDetailById(Long id);

    /**
     * 根据用户名，获取用户详情
     */
    MyUserDetail getUserDetailByUsername(String username);
}
