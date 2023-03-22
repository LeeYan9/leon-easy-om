package com.lyon.easy.async.task.config.idc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Lyon
 */
@ConfigurationProperties(prefix = "async.batch.task")
@Data
public class IdcProperties {

    private IdcMatchType matchType = IdcMatchType.PREFIX;

    /**
     * example:
     * Map<st,127.0.;128.0></>
     */
    private Map<String,String> matchIpList;

    public enum IdcMatchType {
        /**
         * idc match type
         */
        PREFIX;
    }

}
