package edu.ntu.ai.exam.mapper;

import edu.ntu.ai.exam.model.User;
import org.apache.ibatis.annotations.*;


@Mapper
public interface UserMapper {
    @Select("SELECT COALESCE(MAX(id), 0) + 1 FROM users")
    Long getNextId();

    @Insert("INSERT INTO users (id, username, password, role) VALUES (#{id}, #{username}, #{password}, #{role})")
    void insert(User user);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

}