SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for lyon_batch_task
-- ----------------------------
DROP TABLE IF EXISTS `lyon_batch_task`;
CREATE TABLE `lyon_batch_task` (
                                   `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                   `batch_no` varchar(64) NOT NULL COMMENT '任务批次号',
                                   `job_name` varchar(128) NOT NULL COMMENT '任务名称',
                                   `display_name` varchar(255) NOT NULL COMMENT '任务显示名称',
                                   `src` varchar(32) NOT NULL COMMENT '来源',
                                   `group_name` varchar(32) NOT NULL COMMENT '任务组名称',
                                   `idc_type` tinyint unsigned NOT NULL COMMENT '机房消费类型 [0:任意某个机房消费,1:所有机房都必须消费]',
                                   `exec_status` int unsigned NOT NULL COMMENT '任务执行状态[-1:失败，0:初始化，1:执行成功,2:运行中，3:部分执行成功]',
                                   `dependOns` varchar(1024) DEFAULT NULL COMMENT '上下游依赖信息',
                                   `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
                                   `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
                                   `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB ;

-- ----------------------------
-- Table structure for lyon_sub_task
-- ----------------------------
DROP TABLE IF EXISTS `lyon_sub_task`;
CREATE TABLE `lyon_sub_task` (
                                 `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                 `batch_task_id` bigint unsigned NOT NULL COMMENT '批次任务id',
                                 `job_no` varchar(64) CHARACTER SET utf8mb4  NOT NULL COMMENT '子任务编号',
                                 `param` varchar(1024) DEFAULT NULL COMMENT '任务参数',
                                 `task_address` varchar(64) CHARACTER SET utf8mb4  NOT NULL COMMENT '任务地址 bean://{{beanName}} class://{{className}}',
                                 `group_name` varchar(32) NOT NULL COMMENT '任务组名称',
                                 `idc_type` tinyint unsigned NOT NULL COMMENT '机房消费类型 [0:任意某个机房消费,1:所有机房都必须消费]',
                                 `idc` varchar(10) CHARACTER SET utf8mb4  DEFAULT NULL COMMENT '指定机房名称',
                                 `exec_status` int unsigned NOT NULL COMMENT '任务执行状态[-1:失败，0:初始化，1:执行成功,2:运行中]',
                                 `owner` varchar(64) CHARACTER SET utf8mb4  DEFAULT NULL COMMENT '执行者',
                                 `client_id` varchar(10) CHARACTER SET utf8mb4  DEFAULT NULL COMMENT '执行机器标识',
                                 `lock_status` tinyint unsigned DEFAULT NULL COMMENT '锁状态[1:已锁,0:无锁]',
                                 `lock_expire_at` datetime DEFAULT NULL COMMENT '锁失效时间',
                                 `result` varchar(1024) CHARACTER SET utf8mb4  DEFAULT NULL COMMENT '执行结果',
                                 `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
                                 `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
                                 `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 KEY `idx_batch_task_id` (`batch_task_id`) USING BTREE
) ENGINE=InnoDB ;

SET FOREIGN_KEY_CHECKS = 1;
