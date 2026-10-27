package com.xiaocang.toolcall_test.mapper;

import com.xiaocang.toolcall_test.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户表查询。keyword 对 username / nickname 做模糊匹配，status 精确过滤。
 * 分页用 LIMIT #{limit} OFFSET #{offset}，H2 / MySQL 8 都支持。
 */
@Mapper
public interface UserMapper {

    @Select("""
            <script>
            SELECT id, username, nickname, email, status, create_time
            FROM sys_user
            <where>
                <if test="keyword != null and keyword != ''">
                    AND (username LIKE CONCAT('%', #{keyword}, '%')
                      OR nickname LIKE CONCAT('%', #{keyword}, '%'))
                </if>
                <if test="status != null and status != ''">
                    AND status = #{status}
                </if>
            </where>
            ORDER BY id
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<User> searchUsers(@Param("keyword") String keyword,
                           @Param("status") String status,
                           @Param("offset") int offset,
                           @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM sys_user
            <where>
                <if test="keyword != null and keyword != ''">
                    AND (username LIKE CONCAT('%', #{keyword}, '%')
                      OR nickname LIKE CONCAT('%', #{keyword}, '%'))
                </if>
                <if test="status != null and status != ''">
                    AND status = #{status}
                </if>
            </where>
            </script>
            """)
    long countUsers(@Param("keyword") String keyword,
                    @Param("status") String status);
}
