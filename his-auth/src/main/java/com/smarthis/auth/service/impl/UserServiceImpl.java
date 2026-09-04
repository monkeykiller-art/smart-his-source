package com.smarthis.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.auth.dto.request.CreateUserRequest;
import com.smarthis.auth.dto.response.UserVo;
import com.smarthis.auth.entity.AuthRole;
import com.smarthis.auth.entity.AuthUser;
import com.smarthis.auth.entity.AuthUserRole;
import com.smarthis.auth.mapper.RoleMapper;
import com.smarthis.auth.mapper.UserMapper;
import com.smarthis.auth.mapper.UserRoleMapper;
import com.smarthis.auth.service.UserService;
import com.smarthis.auth.support.PasswordHasher;
import com.smarthis.common.context.UserContextHolder;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public UserVo createUser(CreateUserRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<AuthUser>().eq(AuthUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "username already exists");
        }

        AuthUser user = new AuthUser();
        user.setId(Math.abs(UUID.randomUUID().getMostSignificantBits()));
        user.setUsername(request.getUsername());
        user.setPasswordHash(PasswordHasher.hash(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmployeeNo(request.getEmployeeNo());
        user.setUserType(request.getUserType() != null ? request.getUserType() : "EMPLOYEE");
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDeptId(request.getDeptId());
        user.setDeptName(request.getDeptName());
        user.setUserStatus("ACTIVE");
        user.setPrescribeRight(request.getPrescribeRight() != null ? request.getPrescribeRight() : 0);
        user.setAntibioticLevel(request.getAntibioticLevel() != null ? request.getAntibioticLevel() : 0);
        user.setFailedAttempts(0);
        user.setPwdUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        return toVo(user, List.of());
    }

    @Override
    public UserVo getUserById(Long id) {
        AuthUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(id);
        return toVo(user, roleCodes);
    }

    @Override
    public UserVo getUserByUsername(String username) {
        AuthUser user = userMapper.selectOne(
                new LambdaQueryWrapper<AuthUser>().eq(AuthUser::getUsername, username));
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getId());
        return toVo(user, roleCodes);
    }

    @Override
    public PageResult<UserVo> listUsers(String keyword, PageQuery pageQuery) {
        LambdaQueryWrapper<AuthUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(AuthUser::getUsername, keyword)
                    .or().like(AuthUser::getRealName, keyword)
                    .or().like(AuthUser::getEmployeeNo, keyword));
        }
        wrapper.orderByAsc(AuthUser::getUsername);

        Page<AuthUser> page = userMapper.selectPage(pageQuery.toPage(), wrapper);
        List<UserVo> records = page.getRecords().stream()
                .map(u -> toVo(u, userMapper.selectRoleCodesByUserId(u.getId())))
                .collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal(), pageQuery.getPage(), pageQuery.getSize());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(
                new LambdaQueryWrapper<AuthUserRole>().eq(AuthUserRole::getUserId, userId));
        String operator = UserContextHolder.getUsername();
        for (Long roleId : roleIds) {
            AuthUserRole ur = new AuthUserRole();
            ur.setId(Math.abs(UUID.randomUUID().getMostSignificantBits()));
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setCreatedBy(operator);
            ur.setCreatedTime(LocalDateTime.now());
            userRoleMapper.insert(ur);
        }
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        AuthUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!PasswordHasher.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }
        if (PasswordHasher.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_SAME);
        }
        AuthUser update = new AuthUser();
        update.setId(userId);
        update.setPasswordHash(PasswordHasher.hash(newPassword));
        update.setPwdUpdatedAt(LocalDateTime.now());
        update.setUpdatedBy(UserContextHolder.getUsername());
        update.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(update);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        AuthUser update = new AuthUser();
        update.setId(userId);
        update.setPasswordHash(PasswordHasher.hash(newPassword));
        update.setPwdUpdatedAt(LocalDateTime.now());
        update.setUpdatedBy(UserContextHolder.getUsername());
        update.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(update);
    }

    @Override
    public void lockUser(Long userId) {
        AuthUser update = new AuthUser();
        update.setId(userId);
        update.setUserStatus("LOCKED");
        update.setUpdatedBy(UserContextHolder.getUsername());
        update.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(update);
    }

    @Override
    public void unlockUser(Long userId) {
        AuthUser update = new AuthUser();
        update.setId(userId);
        update.setUserStatus("ACTIVE");
        update.setFailedAttempts(0);
        update.setLockedUntil(null);
        update.setUpdatedBy(UserContextHolder.getUsername());
        update.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(update);
    }

    private UserVo toVo(AuthUser user, List<String> roleCodes) {
        return UserVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .employeeNo(user.getEmployeeNo())
                .userType(user.getUserType())
                .realName(user.getRealName())
                .gender(user.getGender())
                .phone(user.getPhone())
                .email(user.getEmail())
                .deptId(user.getDeptId())
                .deptName(user.getDeptName())
                .userStatus(user.getUserStatus())
                .prescribeRight(user.getPrescribeRight())
                .antibioticLevel(user.getAntibioticLevel())
                .roleCodes(roleCodes)
                .build();
    }
}
