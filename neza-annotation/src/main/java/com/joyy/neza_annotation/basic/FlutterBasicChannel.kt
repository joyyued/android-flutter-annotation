package com.joyy.neza_annotation.basic;

import com.joyy.neza_annotation.model.ChannelType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/13 3:19 下午
 * @email: 56002982@qq.com
 * @des: flutter basic channel 的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface FlutterBasicChannel {
    Class<?> codecClass();

    String channelName();

    ChannelType type();
}