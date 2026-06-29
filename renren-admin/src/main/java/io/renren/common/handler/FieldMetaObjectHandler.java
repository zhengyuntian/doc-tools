/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.security.user.UserDetail;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class FieldMetaObjectHandler implements MetaObjectHandler {
    private final static String CREATE_DATE = "createDate";
    private final static String CREATOR = "creator";
    private final static String UPDATE_DATE = "updateDate";
    private final static String UPDATER = "updater";
    private final static String DEPT_ID = "deptId";

    private final static String CREATE_TIME = "createTime";
    private final static String CREATOR_NAME = "creatorName";
    private final static String UPDATE_TIME = "updateTime";
    private final static String UPDATER_NAME = "updaterName";
    private final static String DEL_FLAG = "delFlag";

    @Override
    public void insertFill(MetaObject metaObject) {
        UserDetail user = SecurityUser.getUser();
        Date date = new Date();

        strictInsertFill(metaObject, CREATOR, Long.class, user.getId());
        strictInsertFill(metaObject, CREATE_DATE, Date.class, date);
        strictInsertFill(metaObject, DEPT_ID, Long.class, user.getDeptId());
        strictInsertFill(metaObject, UPDATER, Long.class, user.getId());
        strictInsertFill(metaObject, UPDATE_DATE, Date.class, date);

        if (metaObject.hasGetter(CREATOR_NAME) && metaObject.getGetterType(CREATOR_NAME) == String.class) {
            strictInsertFill(metaObject, CREATOR_NAME, String.class, user.getRealName());
        }
        if (metaObject.hasGetter(CREATE_TIME) && metaObject.getGetterType(CREATE_TIME) == Date.class) {
            strictInsertFill(metaObject, CREATE_TIME, Date.class, date);
        }
        if (metaObject.hasGetter(UPDATER_NAME) && metaObject.getGetterType(UPDATER_NAME) == String.class) {
            strictInsertFill(metaObject, UPDATER_NAME, String.class, user.getRealName());
        }
        if (metaObject.hasGetter(UPDATE_TIME) && metaObject.getGetterType(UPDATE_TIME) == Date.class) {
            strictInsertFill(metaObject, UPDATE_TIME, Date.class, date);
        }
        if (metaObject.hasGetter(DEL_FLAG) && metaObject.getGetterType(DEL_FLAG) == Integer.class) {
            strictInsertFill(metaObject, DEL_FLAG, Integer.class, 0);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, UPDATER, Long.class, SecurityUser.getUserId());
        strictUpdateFill(metaObject, UPDATE_DATE, Date.class, new Date());

        UserDetail user = SecurityUser.getUser();
        Date date = new Date();

        if (metaObject.hasGetter(UPDATER_NAME) && metaObject.getGetterType(UPDATER_NAME) == String.class) {
            strictUpdateFill(metaObject, UPDATER_NAME, String.class, user.getRealName());
        }
        if (metaObject.hasGetter(UPDATE_TIME) && metaObject.getGetterType(UPDATE_TIME) == Date.class) {
            strictUpdateFill(metaObject, UPDATE_TIME, Date.class, date);
        }
    }
}