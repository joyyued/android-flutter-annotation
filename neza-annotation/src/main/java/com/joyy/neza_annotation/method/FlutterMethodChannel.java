package com.joyy.neza_annotation.method;

import com.joyy.neza_annotation.MethodChannelType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface FlutterMethodChannel {
    MethodChannelType type();

    String channelName();
}