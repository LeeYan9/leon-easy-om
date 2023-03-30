package com.lyon.easy.async.task.converter;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import com.lyon.easy.async.task.data.BatchTask;
import com.lyon.easy.async.task.data.SubTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Lyon
 */
@Mapper
public interface BatchTaskConverter {

    BatchTaskConverter INSTANCE = Mappers.getMapper(BatchTaskConverter.class);

    /**
     * convert batchTask to BatchTaskDO
     *
     * @param batchTask task
     * @return result
     */
    @Mappings({
            @Mapping(source = "dependOns", target = "dependOns", qualifiedByName = "listToStr")
    })
    BatchTaskDO from(BatchTask batchTask);

    /**
     * convert BatchTaskDO to BatchTask
     *
     * @param batchTaskDO task
     * @return result
     */
    @Mappings({
            @Mapping(source = "dependOns", target = "dependOns", qualifiedByName = "strToList"),
            @Mapping(target = "execRecord", source = "batchTaskDO", qualifiedByName = "batchTaskDO_to_record")

    })
    BatchTask to(BatchTaskDO batchTaskDO);


    @Named("listToStr")
    default <T> String listToStr(List<T> coll) {
        return JSONUtil.toJsonStr(coll);
    }

    @Named("strToList")
    default <T> List<T> strToList(String str) {
        final Type type = new TypeReference<List<T>>() {
        }.getType();
        return JSONUtil.toBean(str, type, true);
    }


    @Named("batchTaskDO_to_record")
    BatchTask.ExecRecord toExecRecord(BatchTaskDO subTaskDO);

}
