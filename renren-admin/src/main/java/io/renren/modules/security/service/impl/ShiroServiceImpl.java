/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.security.service.impl;

import cn.hutool.core.util.StrUtil;
import io.renren.common.redis.RedisKeys;
import io.renren.common.redis.RedisUtils;
import io.renren.modules.security.dao.SysUserTokenDao;
import io.renren.modules.security.entity.SysUserTokenEntity;
import io.renren.modules.security.service.ShiroService;
import io.renren.modules.security.user.UserDetail;
import io.renren.modules.sys.dao.SysMenuDao;
import io.renren.modules.sys.dao.SysRoleDataScopeDao;
import io.renren.modules.sys.dao.SysUserDao;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.enums.SuperAdminEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShiroServiceImpl implements ShiroService {
    private static final Logger logger = LoggerFactory.getLogger(ShiroServiceImpl.class);

    private final SysMenuDao sysMenuDao;
    private final SysUserDao sysUserDao;
    private final SysUserTokenDao sysUserTokenDao;
    private final SysRoleDataScopeDao sysRoleDataScopeDao;
    private final RedisUtils redisUtils;
    private final boolean redisOpen;

    public ShiroServiceImpl(SysMenuDao sysMenuDao, SysUserDao sysUserDao,
                           SysUserTokenDao sysUserTokenDao, SysRoleDataScopeDao sysRoleDataScopeDao,
                           RedisUtils redisUtils,
                           @Value("${renren.redis.open: false}") boolean redisOpen) {
        this.sysMenuDao = sysMenuDao;
        this.sysUserDao = sysUserDao;
        this.sysUserTokenDao = sysUserTokenDao;
        this.sysRoleDataScopeDao = sysRoleDataScopeDao;
        this.redisUtils = redisUtils;
        this.redisOpen = redisOpen;
    }

    @Override
    public Set<String> getUserPermissions(UserDetail user) {
        String cacheKey = RedisKeys.getUserPermissionsKey(user.getId());

        if (redisOpen) {
            try {
                @SuppressWarnings("unchecked")
                Set<String> cachedPerms = (Set<String>) redisUtils.get(cacheKey);
                if (cachedPerms != null) {
                    return cachedPerms;
                }
            } catch (Exception e) {
                logger.warn("Redis缓存读取失败，降级到数据库查询: {}", e.getMessage());
            }
        }

        List<String> permissionsList;
        if (user.getSuperAdmin() == SuperAdminEnum.YES.value()) {
            permissionsList = sysMenuDao.getPermissionsList();
        } else {
            permissionsList = sysMenuDao.getUserPermissionsList(user.getId());
        }

        Set<String> permsSet = new HashSet<>();
        for (String permissions : permissionsList) {
            if (StrUtil.isBlank(permissions)) {
                continue;
            }
            permsSet.addAll(Arrays.asList(permissions.trim().split(",")));
        }

        if (redisOpen) {
            try {
                redisUtils.set(cacheKey, permsSet, 3600 * 2);
            } catch (Exception e) {
                logger.warn("Redis缓存写入失败: {}", e.getMessage());
            }
        }

        return permsSet;
    }

    @Override
    public SysUserTokenEntity getByToken(String token) {
        String cacheKey = RedisKeys.getUserTokenKey(token);

        if (redisOpen) {
            try {
                SysUserTokenEntity cachedToken = (SysUserTokenEntity) redisUtils.get(cacheKey);
                if (cachedToken != null) {
                    if (cachedToken.getExpireDate().getTime() >= System.currentTimeMillis()) {
                        return cachedToken;
                    }
                    try {
                        redisUtils.delete(cacheKey);
                    } catch (Exception e) {
                        logger.warn("Redis缓存删除失败: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.warn("Redis缓存读取失败，降级到数据库查询: {}", e.getMessage());
            }
        }

        SysUserTokenEntity tokenEntity = sysUserTokenDao.getByToken(token);

        if (redisOpen && tokenEntity != null) {
            try {
                int expireSeconds = (int) ((tokenEntity.getExpireDate().getTime() - System.currentTimeMillis()) / 1000);
                if (expireSeconds > 0) {
                    redisUtils.set(cacheKey, tokenEntity, expireSeconds);
                }
            } catch (Exception e) {
                logger.warn("Redis缓存写入失败: {}", e.getMessage());
            }
        }

        return tokenEntity;
    }

    @Override
    public SysUserEntity getUser(Long userId) {
        String cacheKey = RedisKeys.getUserInfoKey(userId);

        if (redisOpen) {
            try {
                SysUserEntity cachedUser = (SysUserEntity) redisUtils.get(cacheKey);
                if (cachedUser != null) {
                    return cachedUser;
                }
            } catch (Exception e) {
                logger.warn("Redis缓存读取失败，降级到数据库查询: {}", e.getMessage());
            }
        }

        SysUserEntity userEntity = sysUserDao.selectById(userId);

        if (redisOpen && userEntity != null) {
            try {
                redisUtils.set(cacheKey, userEntity, 3600);
            } catch (Exception e) {
                logger.warn("Redis缓存写入失败: {}", e.getMessage());
            }
        }

        return userEntity;
    }

    @Override
    public List<Long> getDataScopeList(Long userId) {
        return sysRoleDataScopeDao.getDataScopeList(userId);
    }

    public void clearUserCache(Long userId) {
        if (redisOpen) {
            try {
                redisUtils.delete(RedisKeys.getUserInfoKey(userId));
                redisUtils.delete(RedisKeys.getUserPermissionsKey(userId));
            } catch (Exception e) {
                logger.warn("Redis缓存清除失败: {}", e.getMessage());
            }
        }
    }

    public void clearTokenCache(String token) {
        if (redisOpen) {
            try {
                redisUtils.delete(RedisKeys.getUserTokenKey(token));
            } catch (Exception e) {
                logger.warn("Redis缓存清除失败: {}", e.getMessage());
            }
        }
    }
}