/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 *
 * Copyright 2005-2006 Sun Microsystems, Inc. All Rights Reserved.
 */


package javax.annotation;
import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * The Generated annoation is used to mark source code that has been generated.
 * It can also be used to differentiate user written code from generated code
 * in a single file. When used, the value element must have the name of the 
 * code generator. The recommended convention is to use the fully qualified 
 * name of the code generator in the value field . 
 * For example: com.company.package.classname.
 * The date element is used to indicate the date the source was generated. 
 * The date element must follow the ISO 8601 standard. For example the date 
 * element would have the following value 2001-07-04T12:08:56.235-0700
 * which represents 2001-07-04 12:08:56 local time in the U.S. Pacific 
 * Time time zone.
 * The comment element is a place holder for any comments that the code 
 * generator may want to include in the generated code.
 * 
 * @since Common Annotations 1.0
 */

@Documented
@Retention(SOURCE)
@Target({PACKAGE, TYPE, ANNOTATION_TYPE, METHOD, CONSTRUCTOR, FIELD, 
        LOCAL_VARIABLE, PARAMETER})
public @interface Generated {
   /**
    * This is used by the code generator to mark the generated classes
    * and methods.
    */
   String[] value();

   /**
    * Date when the source was generated.
    */
   String date() default "";

   /**
    * A place holder for any comments that the code generator may want to 
    * include in the generated code.
    */
   String comments() default "";
}

