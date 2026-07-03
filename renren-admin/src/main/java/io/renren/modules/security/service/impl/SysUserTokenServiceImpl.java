/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.security.service.impl;

import io.renren.common.constant.Constant;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.modules.security.oauth2.TokenGenerator;
import io.renren.common.utils.Result;
import io.renren.modules.security.dao.SysUserTokenDao;
import io.renren.modules.security.entity.SysUserTokenEntity;
import io.renren.modules.security.service.ShiroService;
import io.renren.modules.security.service.SysUserTokenService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SysUserTokenServiceImpl extends BaseServiceImpl<SysUserTokenDao, SysUserTokenEntity> implements SysUserTokenService {
	/**
	 * 12小时后过期
	 */
	private final static int EXPIRE = 3600 * 12;

	@Resource
	private ShiroService shiroService;

	@Override
	public Result createToken(Long userId) {
		String token;
		Date now = new Date();
		Date expireTime = new Date(now.getTime() + EXPIRE * 1000);

		SysUserTokenEntity tokenEntity = baseDao.getByUserId(userId);
		if(tokenEntity == null){
			token = TokenGenerator.generateValue();

			tokenEntity = new SysUserTokenEntity();
			tokenEntity.setUserId(userId);
			tokenEntity.setToken(token);
			tokenEntity.setUpdateDate(now);
			tokenEntity.setExpireDate(expireTime);

			this.insert(tokenEntity);
		}else{
			String oldToken = tokenEntity.getToken();
			if(tokenEntity.getExpireDate().getTime() < System.currentTimeMillis()){
				token = TokenGenerator.generateValue();
				shiroService.clearTokenCache(oldToken);
			}else {
				token = tokenEntity.getToken();
			}

			tokenEntity.setToken(token);
			tokenEntity.setUpdateDate(now);
			tokenEntity.setExpireDate(expireTime);

			this.updateById(tokenEntity);
		}

		shiroService.clearUserCache(userId);

		Map<String, Object> map = new HashMap<>(2);
		map.put(Constant.TOKEN_HEADER, token);
		map.put("expire", EXPIRE);
		return new Result().ok(map);
	}

	@Override
	public void logout(Long userId) {
		String token = TokenGenerator.generateValue();

		baseDao.updateToken(userId, token);

		shiroService.clearUserCache(userId);
	}
}