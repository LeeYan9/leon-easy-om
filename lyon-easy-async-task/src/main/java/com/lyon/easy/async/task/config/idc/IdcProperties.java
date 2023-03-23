package com.lyon.easy.async.task.config.idc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @author Lyon
 */
@ConfigurationProperties(prefix = "async-task.idc")
@Data
public class IdcProperties {

    private IdcMatchType matchType = IdcMatchType.PREFIX;

    /**
     * example:
     * Map<st,127.0.;128.0></>
     */
    private Map<String, List<String>> matchIpList;

    public enum IdcMatchType {
        /**
         * idc match type
         */
        PREFIX;
    }

}
