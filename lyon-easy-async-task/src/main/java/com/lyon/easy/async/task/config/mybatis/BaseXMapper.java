package com.lyon.easy.async.task.config.mybatis;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Collection;

/**
 * @author Lyon
 */
@SuppressWarnings({"AlibabaClassNamingShouldBeCamel"})
public interface BaseXMapper<T> extends BaseMapper<T> {

    /**
     * 批量插入 仅适用于mysql
     *
     * @param entityList 实体列表
     * @return 影响行数
     */
    Integer insertBatchSomeColumn(Collection<T> entityList);
//
//    /**
//     * 批量插入
//     * @param entityList 实体列表
//     * @return 影响行数
//     */
//    default Integer insertBatch(Collection<T> entityList){
//        if (entityList.size() == 1) {
//           return insert(CollUtil.getFirst(entityList));
//        }
//        return insertBatchSomeColumn(entityList);
//    }

}
