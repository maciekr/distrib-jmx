package com.heyitworks.util.distribjmx.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.heyitworks.util.distribjmx.handlers.NoopResultHandler;
import com.heyitworks.util.distribjmx.handlers.ResultHandler;

/**
 * 
 * @author mrakowicz
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })
public @interface Distributed {
	Class<? extends ResultHandler<?>> handler() default NoopResultHandler.class;
}
