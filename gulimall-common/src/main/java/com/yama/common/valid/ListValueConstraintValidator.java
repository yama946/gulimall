package com.yama.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * 自定义校验器
 *
 * 如何自定义校验器？
 * 1、必须实现public interface ConstraintValidator<A extends Annotation, T>接口
 * 接口中包含两个泛型：第一个泛型--->指定关联的注解，第二个泛型--->指定要检验的数据类型
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {
    /**
     * 初始化方法：用来获取注解中的详细信息，比如注解中子定义传递的值
     * @param constraintAnnotation
     */

    private Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(ListValue constraintAnnotation) {
        //获取注解中vals属性中的值
        int[] vals = constraintAnnotation.vals();

        for (Integer val : vals){
            set.add(val);
        }
    }

    /**
     * 判断是否校验成功
     * @param value 提交的需要校验的值
     * @param context
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        //判断提交的值，是否是指定值，否则返回false
//        if (set.contains(value))
        return set.contains(value);
    }
}
