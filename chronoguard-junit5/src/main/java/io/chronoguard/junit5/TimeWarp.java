package io.chronoguard.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
public @interface TimeWarp {
    String freezeAt() default "";
    String offsetBy() default "";
}
