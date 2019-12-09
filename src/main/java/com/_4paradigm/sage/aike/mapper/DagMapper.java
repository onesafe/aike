package com._4paradigm.sage.aike.mapper;

import com._4paradigm.sage.aike.entity.DagDMO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * Created by wangyiping on 2019/12/9 6:24 PM.
 */
@Mapper
@Repository
public interface DagMapper {

    @Insert("insert into dag (dag_name, dag_content) values (#{dagDMO.dagName}, #{dagDMO.dagContent})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "dagDMO.id")
    int insert(@Param("dagDMO") DagDMO dagDMO);


    @Select("select id, dag_name, dag_content from dag where id=#{id}")
    @Results({
            @Result(property = "dagName", column = "dag_name"),
            @Result(property = "dagContent", column = "dag_content")
    })
    DagDMO select(@Param("id") Long id);


    @Delete("delete from dag where id=#{id}")
    void delete(@Param("id") Long id);
}
