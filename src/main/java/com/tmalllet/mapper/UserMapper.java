package com.tmalllet.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.tmalllet.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmalllet.entity.User
 */
public interface UserMapper extends BaseMapper<User> {
    List<User> selectAll();

    int countByName(@Param("name") String name);

    User getOneByName(@Param("name") String name);

    User getOneByNameAndPassword(@Param("name") String name, @Param("password") String password);
}




