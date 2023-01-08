package com.my.pack.webService.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @ interface OnStartup
{
public int priority();
}
