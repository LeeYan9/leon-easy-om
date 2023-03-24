package com.lyon.easy.async.task.converter;

import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import com.lyon.easy.async.task.data.SubTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
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
     * @param subTask     task
     * @param batchTaskDO batch task
     * @return result
     */
    @Mappings({
            @Mapping(source = "batchTaskDO.id", target = "batchTaskId"),
            @Mapping(source = "batchTaskDO.groupName", target = "groupName"),
    })
    SubTaskDO from(SubTask subTask, BatchTaskDO batchTaskDO);


    /**
     * convert subTask to SubTaskDO
     *
     * @param subTask     task
     * @param batchTaskDO batchTaskDO
     * @param idc         idc
     * @return result
     */
    @Mappings({
            @Mapping(source = "batchTaskDO.id", target = "batchTaskId"),
            @Mapping(source = "batchTaskDO.groupName", target = "groupName"),
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
//            @Mapping(target = "record.id", source = "id"),
//            @Mapping(target = "record.batchTaskId", source = "batchTaskId"),
//            @Mapping(target = "record.idcType", source = "idcType"),
//            @Mapping(target = "record.owner", source = "owner"),
//            @Mapping(target = "record.clientId", source = "clientId"),
//            @Mapping(target = "record.lockStatus", source = "lockStatus"),
            @Mapping(target = "execRecord", source = "subTaskDO", qualifiedByName = "subTaskDO_to_record")
    })
    SubTask to(SubTaskDO subTaskDO);

//    @Named("subTaskDO_to_record")
//    default SubTask.ExecRecord subTaskDO_to_record(SubTaskDO subTaskDO){
//        to
//    }

    @Named("subTaskDO_to_record")
    SubTask.ExecRecord toExecRecord(SubTaskDO subTaskDO);

}
