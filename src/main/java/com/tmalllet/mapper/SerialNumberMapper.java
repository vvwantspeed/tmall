package com.tmalllet.mapper;
import org.apache.ibatis.annotations.Param;

import com.tmalllet.entity.SerialNumber;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity gene.entity.SerialNumber
 */
public interface SerialNumberMapper extends BaseMapper<SerialNumber> {
    SerialNumber selectOneByName(@Param("name") String name);
}




