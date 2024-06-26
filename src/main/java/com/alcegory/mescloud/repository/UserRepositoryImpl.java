package com.alcegory.mescloud.repository;

import com.alcegory.mescloud.model.entity.UserEntity;
import com.alcegory.mescloud.model.filter.Filter;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl extends AbstractFilterRepository<Filter.Property, UserEntity> {
    
    public List<UserEntity> getFilteredUsers(Filter filter) {
        return super.findAllWithFilter(filter, UserEntity.class);
    }
}
