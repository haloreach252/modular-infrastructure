package com.miniverse.modularinfrastructure.api.utils;

import org.apache.logging.log4j.Logger;

/**
 * A logger that can be enabled/disabled
 */
public class GatedLogger {
    private final Logger logger;
    private final boolean enabled;
    
    public GatedLogger(Logger logger, boolean enabled) {
        this.logger = logger;
        this.enabled = enabled;
    }
    
    public void info(String message, Object... params) {
        if (enabled) {
            logger.info(message, params);
        }
    }
    
    public void warn(String message, Object... params) {
        if (enabled) {
            logger.warn(message, params);
        }
    }
    
    public void error(String message, Object... params) {
        if (enabled) {
            logger.error(message, params);
        }
    }
    
    public void debug(String message, Object... params) {
        if (enabled) {
            logger.debug(message, params);
        }
    }
}