package com.lyon.easy.limiter.common;

/**
 * @author Lyon
 */
public class SystemClock {

    public static long now(){
        return System.currentTimeMillis();
    }

    public static long now_seconds(){
        return now()/1000;
    }
}
