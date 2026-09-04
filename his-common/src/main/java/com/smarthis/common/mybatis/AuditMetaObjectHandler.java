package com.smarthis.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.smarthis.common.context.UserContextHolder;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        String username = UserContextHolder.getUsername();
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createdBy", String.class, username);
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedBy", String.class, username);
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedBy", String.class, UserContextHolder.getUsername());
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }
}
