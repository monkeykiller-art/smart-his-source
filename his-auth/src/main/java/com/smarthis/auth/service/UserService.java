package com.smarthis.auth.service;

import com.smarthis.auth.dto.request.CreateUserRequest;
import com.smarthis.auth.dto.response.UserVo;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;

import java.util.List;

public interface UserService {

    UserVo createUser(CreateUserRequest request);

    UserVo getUserById(Long id);

    UserVo getUserByUsername(String username);

    PageResult<UserVo> listUsers(String keyword, PageQuery pageQuery);

    void assignRoles(Long userId, List<Long> roleIds);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void resetPassword(Long userId, String newPassword);

    void lockUser(Long userId);

    void unlockUser(Long userId);
}
