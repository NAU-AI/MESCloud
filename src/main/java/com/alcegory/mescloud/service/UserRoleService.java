package com.alcegory.mescloud.service;

import com.alcegory.mescloud.security.model.UserRoleEntity;

import java.util.List;
import java.util.Optional;

public interface UserRoleService {

    void saveUserRole(Long userId, Long roleId, Long sectionId);

    void updateUserRole(Long userRoleId, Long roleId, Long sectionId);

    void deleteUserRoleById(Long userRoleId);

    List<UserRoleEntity> findByUser(Long userId);

    List<UserRoleEntity> findByUserId(Long userId);

    UserRoleEntity findUserRoleByUserAndSection(Long userId, Long sectionId);
}
