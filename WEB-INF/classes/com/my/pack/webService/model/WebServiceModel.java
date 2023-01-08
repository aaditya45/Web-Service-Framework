package com.my.pack.webService.model;
import com.my.pack.webService.pojo.*;
import java.util.*;
public class WebServiceModel
{
private HashMap<String,Service> serviceMap;
public WebServiceModel()
{
serviceMap=new HashMap<String,Service>();
}

public void addInMap(String path,Service service)
{
this.serviceMap.put(path,service);
}

public HashMap<String,Service> getServiceMap()
{
return this.serviceMap;
}

}//class ends