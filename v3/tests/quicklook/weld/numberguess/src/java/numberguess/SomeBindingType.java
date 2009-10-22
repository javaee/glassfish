package numberguess;

import javax.interceptor.*;

import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Inherited;

@Inherited
@InterceptorBinding
@Target(TYPE)
@Retention(RUNTIME)
public @interface SomeBindingType {}
