package com.lyon.easy.async.task.converter;

import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import com.lyon.easy.async.task.data.SubTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * @author Lyon
 */
@Mapper
public interface SubTaskConverter {

    SubTaskConverter INSTANCE = Mappers.getMapper(SubTaskConverter.class);

    /**
     * convert subTask to SubTaskDO
     *
     * @param subTask task
     * @param batchTaskDO batch task
     * @return result
     */
    @Mappings({
            @Mapping(source = "batchTaskId", target = "batchTaskDO.id"),
            @Mapping(source = "groupName", target = "batchTaskDO.groupName"),
//            @Mapping(source = "status" , target = "")
    })
    SubTaskDO from(BatchTaskDO batchTaskDO, SubTask subTask);



    /**
     * convert subTask to SubTaskDO
     *
     * @param subTask task
     * @param batchTaskDO batchTaskDO
     * @param idc idc
     * @return result
     */
    @Mappings({
            @Mapping(source = "batchTaskId", target = "batchTaskDO.id"),
            @Mapping(source = "groupName", target = "batchTaskDO.groupName"),
            @Mapping(source = "idc", target = "idc"),
//            @Mapping(source = "status" , target = "")
    })
    SubTaskDO from(BatchTaskDO batchTaskDO, SubTask subTask, String idc);

    /**
     * convert SubTaskDO to SubTask
     *
     * @param subTaskDO task
     * @return result
     */
    @Mappings({
            @Mapping(source = "record.id", target = "id"),
            @Mapping(source = "record.batchTaskId", target = "batchTaskId"),
            @Mapping(source = "record.idcType", target = "idcType"),
            @Mapping(source = "record.owner", target = "owner"),
            @Mapping(source = "record.clientId", target = "clientId"),
            @Mapping(source = "record.lockStatus", target = "lockStatus"),
            @Mapping(source = "record.lockExpireAt", target = "lockExpireAt"),
    })
    SubTask to(SubTaskDO subTaskDO);

}
