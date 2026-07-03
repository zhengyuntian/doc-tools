/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.modules.security.service.ShiroService;
import io.renren.modules.sys.dao.SysRoleUserDao;
import io.renren.modules.sys.entity.SysRoleUserEntity;
import io.renren.modules.sys.service.SysRoleUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色用户关系
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Service
public class SysRoleUserServiceImpl extends BaseServiceImpl<SysRoleUserDao, SysRoleUserEntity> implements SysRoleUserService {

    @Resource
    private ShiroService shiroService;

    @Override
    public void saveOrUpdate(Long userId, List<Long> roleIdList) {
        deleteByUserIds(new Long[]{userId});

        if(CollUtil.isEmpty(roleIdList)){
            shiroService.clearUserCache(userId);
            return ;
        }

        for(Long roleId : roleIdList){
            SysRoleUserEntity sysRoleUserEntity = new SysRoleUserEntity();
            sysRoleUserEntity.setUserId(userId);
            sysRoleUserEntity.setRoleId(roleId);
            insert(sysRoleUserEntity);
        }

        shiroService.clearUserCache(userId);
    }

    @Override
    public void deleteByRoleIds(Long[] roleIds) {
        baseDao.deleteByRoleIds(roleIds);
    }

    @Override
    public void deleteByUserIds(Long[] userIds) {
        baseDao.deleteByUserIds(userIds);
    }

    @Override
    public List<Long> getRoleIdList(Long userId) {

        return baseDao.getRoleIdList(userId);
    }
}