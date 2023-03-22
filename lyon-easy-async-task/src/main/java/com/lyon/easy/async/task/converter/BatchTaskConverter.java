package com.lyon.easy.async.task.converter;

import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.async.task.data.BatchTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * @author Lyon
 */
@Mapper
public interface BatchTaskConverter {

    BatchTaskConverter INSTANCE = Mappers.getMapper(BatchTaskConverter.class);
    /**
     * convert batchTask to BatchTaskDO
     * @param batchTask task
     * @return result
     */
    @Mappings({
//            @Mapping(source = "" , target = "" , )
    })
    BatchTaskDO from(BatchTask batchTask);

    /**
     * convert BatchTaskDO to BatchTask
     * @param batchTaskDO task
     * @return result
     */
    BatchTask to(BatchTaskDO batchTaskDO);


}
