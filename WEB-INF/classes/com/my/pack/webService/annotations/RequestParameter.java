package com.my.pack.webService.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @ interface RequestParameter
{
public String value();
}