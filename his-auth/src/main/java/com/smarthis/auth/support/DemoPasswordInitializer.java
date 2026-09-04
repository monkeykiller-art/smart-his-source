package com.smarthis.auth.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.auth.entity.AuthUser;
import com.smarthis.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoPasswordInitializer implements ApplicationRunner {

    private static final String PLACEHOLDER = "$2a$PLACEHOLDER";

    private final UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        List<AuthUser> users = userMapper.selectList(
                new LambdaQueryWrapper<AuthUser>()
                        .eq(AuthUser::getPasswordHash, PLACEHOLDER));

        if (users.isEmpty()) {
            return;
        }

        log.info("initializing {} demo user passwords with SHA-256 hash", users.size());
        String defaultPassword = "His@2026";
        String hashed = PasswordHasher.hash(defaultPassword);

        for (AuthUser user : users) {
            AuthUser update = new AuthUser();
            update.setId(user.getId());
            update.setPasswordHash(hashed);
            update.setPwdUpdatedAt(LocalDateTime.now());
            update.setUpdatedBy("system");
            update.setUpdatedTime(LocalDateTime.now());
            userMapper.updateById(update);
            log.info("initialized password for user: {}", user.getUsername());
        }
    }
}
