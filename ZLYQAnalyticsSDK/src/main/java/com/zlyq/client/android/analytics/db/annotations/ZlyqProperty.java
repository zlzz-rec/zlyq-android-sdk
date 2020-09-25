package com.zlyq.client.android.analytics.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZlyqProperty {
	 public String column() default "";
	 public String defaultValue() default "";
}
