package com.yama.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 如何自定义校验注解？
 * 1、满足JSR303要求，必须添加的三个属性。可以参照@NotNull注解
 *     String message() default "{javax.validation.constraints.NotNull.message}";
 *
 *     Class<?>[] groups() default {};
 *
 *     Class<? extends Payload>[] payload() default {};
 * 2.message的默认值是从配置文件ValidationMessages.properties获取，我们在项目中创建本地同名配置文件
 *
 * 3.导入相关的创建注解的依赖
 *        <dependency>
 *             <groupId>javax.validation</groupId>
 *             <artifactId>validation-api</artifactId>
 *             <version>2.0.1.Final</version>
 *         </dependency>
 * 4、添加注解，元注解以及@Constraint注解
 * Class<? extends ConstraintValidator<?, ?>>[] validatedBy();
 * @Constraint：指定所使用的校验器
 *
 * 5、使用注解@Constraint中validatedBy属性指定自定义校验器
 * @Constraint(validatedBy = {ListValueConstraintValidator.class})
 *
 * 当我们校验的类型变成double，我们还可以使用此注解，但是需要在创建自定义校验器进行关联，会自动是被使用配置的多个校验器
 * @Constraint(validatedBy = {ListValueIntegerConstraintValidator.class,ListValueDoubleConstraintValidator.class})
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {ListValueConstraintValidator.class})
public @interface ListValue {
    String message() default "{com.yama.common.valid.ListValue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 自定义属性，传入的值表示指定的值
     * @return
     */
    int[] vals() default {};
}
