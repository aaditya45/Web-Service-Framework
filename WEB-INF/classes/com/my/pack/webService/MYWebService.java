package com.my.pack.webService;
import com.my.pack.webService.annotations.*;
import com.my.pack.webService.pojo.*;
import com.my.pack.webService.model.*;
import com.google.gson.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.annotation.*;
import com.my.pack.webService.annotations.*;

public class MYWebService extends HttpServlet
{
public void doGet(HttpServletRequest request,HttpServletResponse response)
{
PrintWriter pw=null;
try
{
pw=response.getWriter();
ServletContext servletContext=getServletContext();
String requestUrl,key;
Service service;
Class clazz;
Method method;
Object classObject;

WebServiceModel webServiceModel=(WebServiceModel)servletContext.getAttribute("webServiceModel");
if(webServiceModel==null)
{
System.out.println("Model loading problem......");
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
return;
}


requestUrl=request.getRequestURI();
key=requestUrl.substring(requestUrl.indexOf(request.getServletPath())+request.getServletPath().length(),request.getRequestURI().length());
System.out.println(key);


service=webServiceModel.getServiceMap().get(key);
if(service==null)
{
System.out.println("request method not found......");
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}

if(service.getIsGetAllowed()==false)
{
System.out.println("GET method not allowed ......");
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}


System.out.println(service);

clazz=service.getServiceClass();
method=service.getServiceMethod();
classObject=clazz.newInstance();

//securedAccess imple starts
if(service.getIsSecured())
{
Class securedClass=Class.forName(service.getCheckPost());
String securedMethodName=service.getGuard();
Parameter secureMethodParameters=null;
Object guardMethodParameters[]=null;
Class parameterTypesInGuard[]=null;

for(Method guardMethod : securedClass.getMethods())
{
if(guardMethod.getName().equals(securedMethodName))
{
parameterTypesInGuard=guardMethod.getParameterTypes();
for(int i=0;i<parameterTypesInGuard.length;i++)
{
guardMethodParameters=new Object[parameterTypesInGuard.length];
if(parameterTypesInGuard[i].getSimpleName().equals("ApplicationScope"))
{
guardMethodParameters[i]=new ApplicationScope(servletContext);
}
else if(parameterTypesInGuard[i].getSimpleName().equals("ApplicationDirectory"))
{
guardMethodParameters[i]=new ApplicationDirectory(new File(servletContext.getRealPath("")));
}
else if(parameterTypesInGuard[i].getSimpleName().equals("SessionScope"))
{
guardMethodParameters[i]=new SessionScope(request.getSession());
}
else if(parameterTypesInGuard[i].getSimpleName().equals("requestScope"))
{
guardMethodParameters[i]=new RequestScope(request);
}
else
{
//  ////////////  exception
}

}//loop on parameter
try
{
guardMethod.invoke(securedClass.newInstance(),guardMethodParameters);
}catch(InvocationTargetException invocationTargetException)
{
System.out.println("guard sent exception : "+invocationTargetException);
System.out.println("with cause : "+invocationTargetException.getCause());
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
return;
}
break;
}//condition
}//loop on class

}//condition ends
//securedAccess imple ends

//calling inject func

if(service.getInjectSessionScope())
{
Method setSessionScope=clazz.getMethod("setSessionScope",SessionScope.class);
setSessionScope.invoke(classObject,new SessionScope(request.getSession()));
}

if(service.getInjectApplicationScope())
{
Method setApplicationScope=clazz.getMethod("setApplicationScope",ApplicationScope.class);
setApplicationScope.invoke(classObject,new ApplicationScope(getServletContext()));
}

if(service.getInjectRequestScope())
{
Method setRequestScope=clazz.getMethod("setRequestScope",RequestScope.class);
setRequestScope.invoke(classObject,new RequestScope(request));
}

if(service.getInjectApplicationDirectory())
{
//have to do
}

//inje ends here

//auto wired prop
List<AutoWiredService> autoWiredList=service.getAutoWiredList();
String autoWiredAttributeName;
Class autoWiredAttributeType;
Field autoWiredAttributeField;
Object requestObject;
Object sessionScopeObject;
Object applicationScopeObject;
Method autoWiredMethod;
for(AutoWiredService autoWiredService : autoWiredList)
{
autoWiredAttributeName=autoWiredService.getName();
autoWiredAttributeField=autoWiredService.getAutoWiredField();
autoWiredAttributeType=autoWiredAttributeField.getType();

System.out.println(autoWiredAttributeName);
System.out.println(autoWiredAttributeType);
System.out.println(autoWiredAttributeField);
requestObject=request.getAttribute(autoWiredAttributeName);
sessionScopeObject=request.getSession().getAttribute(autoWiredAttributeName);
applicationScopeObject=getServletContext().getAttribute(autoWiredAttributeName);

if(requestObject!=null && autoWiredAttributeType.isInstance(requestObject))
{
autoWiredMethod=clazz.getMethod("set"+autoWiredAttributeField.getName(),autoWiredAttributeType);
autoWiredMethod.invoke(classObject,requestObject);
}
else if(sessionScopeObject!=null && autoWiredAttributeType.isInstance(sessionScopeObject))
{
autoWiredMethod=clazz.getMethod("set"+autoWiredAttributeField.getName(),autoWiredAttributeType);
autoWiredMethod.invoke(classObject,sessionScopeObject);
}
else if(applicationScopeObject!=null && autoWiredAttributeType.isInstance(applicationScopeObject))
{
autoWiredMethod=clazz.getMethod("set"+autoWiredAttributeField.getName(),autoWiredAttributeType);
autoWiredMethod.invoke(classObject,applicationScopeObject);
}
System.out.println("__________________________");
}//list loop ends
//auto wired ends


//setting parameters starts

if(method.getParameterCount()!=service.getRequestParameterList().size())
{
System.out.println("parameter count is not matching with annotation applied parameters.");
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}

String parameterName;
Class parameterType;
String parameterTypeName;
String parameter;
Object requestParameters[]=new Object[service.getRequestParameterList().size()];
int i=0;
for(RequestParameterService requestParameterService : service.getRequestParameterList())
{
parameter=null;
parameterName=requestParameterService.getName();
parameterType=requestParameterService.getParameterType();
parameter=request.getParameter(parameterName);
parameterTypeName=parameterType.toString();

if(requestParameterService.getIsJson())
{
Gson gson=new Gson();
BufferedReader bufferedReader=request.getReader();
StringBuffer stringBuffer=new StringBuffer();
String b;
String rawString;
while(true)
{
b=bufferedReader.readLine();
if(b==null) break;
stringBuffer.append(b);
}
rawString=stringBuffer.toString();
System.out.println(rawString);
requestParameters[i]=gson.fromJson(rawString,parameterType);
}

else if(requestParameterService.getIsApplicationScope())
{
requestParameters[i]=new ApplicationScope(getServletContext());
}
else if(requestParameterService.getIsApplicationDirectory())
{
requestParameters[i]=new ApplicationDirectory(new File(servletContext.getRealPath("")));
}
else if(requestParameterService.getIsSessionScope())
{
requestParameters[i]=new SessionScope(request.getSession());
}
else if(requestParameterService.getIsRequestScope())
{
requestParameters[i]=new RequestScope(request);
}

else if(requestParameterService.getIsPrimitive())
{
if(parameterTypeName.equals("int") || parameterTypeName.equals("Integer"))
{
try
{
requestParameters[i]=Integer.parseInt(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("long") || parameterTypeName.equals("Long"))
{
try
{
requestParameters[i]=Long.parseLong(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("short") || parameterTypeName.equals("Short"))
{
try
{
requestParameters[i]=Short.parseShort(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("double") || parameterTypeName.equals("Double"))
{
try
{
requestParameters[i]=Double.parseDouble(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("boolean")  || parameterTypeName.equals("Boolean"))
{
requestParameters[i]=Boolean.parseBoolean(parameter);
}
else if(parameterTypeName.equals("float") || parameterTypeName.equals("Float"))
{
try
{
requestParameters[i]=Float.parseFloat(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("byte") || parameterTypeName.equals("Byte"))
{
try
{
requestParameters[i]=Byte.parseByte(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.	SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("char") || parameterTypeName.equals("Character"))
{
requestParameters[i]=parameter.charAt(0);
}
else
{
requestParameters[i]=parameter;
}
}
i++;
}//parameter loop ends


//setting parameters ends
//calling method

		System.out.println("Request parameter size "+requestParameters.length);
ServiceResponse serviceResponse=new ServiceResponse();
serviceResponse.setIsSuccess(true);
if(requestParameters.length!=0)
{
serviceResponse.setResult(method.invoke(classObject,requestParameters));
}
else
{
serviceResponse.setResult(method.invoke(classObject));
}
pw.println(new Gson().toJson(serviceResponse));
pw.flush();
}catch(Exception e)
{
System.out.println(e+" "+request.getRequestURI());
System.out.println(e.getCause());
ServiceResponse serviceResponse=new ServiceResponse();
serviceResponse.setIsSuccess(false);
serviceResponse.setException(e.getCause().toString());
pw.println(new Gson().toJson(serviceResponse));
pw.flush();
}
}//function ends



















public void doPost(HttpServletRequest request,HttpServletResponse response)
{
PrintWriter pw=null;
try
{
pw=response.getWriter();
response.setContentType("application/json");
ServletContext servletContext=getServletContext();
String requestUrl,key;
Service service;
Class clazz;
Method method;
Object classObject;


WebServiceModel webServiceModel=(WebServiceModel)servletContext.getAttribute("webServiceModel");
if(webServiceModel==null)
{
System.out.println("Model loading problem......");
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
return;
}


requestUrl=request.getRequestURI();
key=requestUrl.substring(requestUrl.indexOf(request.getServletPath())+request.getServletPath().length(),request.getRequestURI().length());
System.out.println(key);


service=webServiceModel.getServiceMap().get(key);
if(service==null)
{
System.out.println("request method not found......");
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}

if(service.getIsPostAllowed()==false)
{
System.out.println("Post method not allowed ......");
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}


System.out.println(service);

clazz=service.getServiceClass();
method=service.getServiceMethod();
classObject=clazz.newInstance();

//securedAccess imple starts
if(service.getIsSecured())
{
Class securedClass=Class.forName(service.getCheckPost());
String securedMethodName=service.getGuard();
Parameter secureMethodParameters=null;
Object guardMethodParameters[]=null;
Class parameterTypesInGuard[]=null;

for(Method guardMethod : securedClass.getMethods())
{
if(guardMethod.getName().equals(securedMethodName))
{
parameterTypesInGuard=guardMethod.getParameterTypes();
for(int i=0;i<parameterTypesInGuard.length;i++)
{
guardMethodParameters=new Object[parameterTypesInGuard.length];
if(parameterTypesInGuard[i].getSimpleName().equals("ApplicationScope"))
{
guardMethodParameters[i]=new ApplicationScope(servletContext);
}
else if(parameterTypesInGuard[i].getSimpleName().equals("ApplicationDirectory"))
{
guardMethodParameters[i]=new ApplicationDirectory(new File(servletContext.getRealPath("")));
}
else if(parameterTypesInGuard[i].getSimpleName().equals("SessionScope"))
{
guardMethodParameters[i]=new SessionScope(request.getSession());
}
else if(parameterTypesInGuard[i].getSimpleName().equals("requestScope"))
{
guardMethodParameters[i]=new RequestScope(request);
}
else
{
//  ////////////  exception
}

}//loop on parameter
try
{
guardMethod.invoke(securedClass.newInstance(),guardMethodParameters);
}catch(InvocationTargetException invocationTargetException)
{
System.out.println("guard sent exception : "+invocationTargetException);
System.out.println("with cause : "+invocationTargetException.getCause());
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
return;
}
break;
}//condition
}//loop on class

}//condition ends




//securedAccess imple ends

//calling inject func

if(service.getInjectSessionScope())
{
Method setSessionScope=clazz.getMethod("setSessionScope",SessionScope.class);
setSessionScope.invoke(classObject,new SessionScope(request.getSession()));
}

if(service.getInjectApplicationScope())
{
Method setApplicationScope=clazz.getMethod("setApplicationScope",ApplicationScope.class);
setApplicationScope.invoke(classObject,new ApplicationScope(getServletContext()));
}

if(service.getInjectRequestScope())
{
Method setRequestScope=clazz.getMethod("setRequestScope",RequestScope.class);
setRequestScope.invoke(classObject,new RequestScope(request));
}

if(service.getInjectApplicationDirectory())
{
//have to do
}

//inje ends here

//auto wired prop
List<AutoWiredService> autoWiredList=service.getAutoWiredList();
String autoWiredAttributeName;
Class autoWiredAttributeType;
Field autoWiredAttributeField;
Object requestObject;
Object sessionScopeObject;
Object applicationScopeObject;
Method autoWiredMethod;
for(AutoWiredService autoWiredService : autoWiredList)
{
autoWiredAttributeName=autoWiredService.getName();
autoWiredAttributeField=autoWiredService.getAutoWiredField();
autoWiredAttributeType=autoWiredAttributeField.getType();

System.out.println(autoWiredAttributeName);
System.out.println(autoWiredAttributeType);
System.out.println(autoWiredAttributeField);
requestObject=request.getAttribute(autoWiredAttributeName);
sessionScopeObject=request.getSession().getAttribute(autoWiredAttributeName);
applicationScopeObject=getServletContext().getAttribute(autoWiredAttributeName);

if(requestObject!=null && autoWiredAttributeType.isInstance(requestObject))
{
autoWiredMethod=clazz.getMethod("set"+autoWiredAttributeField.getName(),autoWiredAttributeType);
autoWiredMethod.invoke(classObject,requestObject);
}
else if(sessionScopeObject!=null && autoWiredAttributeType.isInstance(sessionScopeObject))
{
autoWiredMethod=clazz.getMethod("set"+autoWiredAttributeField.getName(),autoWiredAttributeType);
autoWiredMethod.invoke(classObject,sessionScopeObject);
}
else if(applicationScopeObject!=null && autoWiredAttributeType.isInstance(applicationScopeObject))
{
autoWiredMethod=clazz.getMethod("set"+autoWiredAttributeField.getName(),autoWiredAttributeType);
autoWiredMethod.invoke(classObject,applicationScopeObject);
}
System.out.println("__________________________");
}//list loop ends
//auto wired ends


//setting parameters starts

if(method.getParameterCount()!=service.getRequestParameterList().size())
{
System.out.println("parameter count is not matching with annotation applied parameters.");
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}

String parameterName;
Class parameterType;
String parameterTypeName;
String parameter;
Object requestParameters[]=new Object[service.getRequestParameterList().size()];
int i=0;
for(RequestParameterService requestParameterService : service.getRequestParameterList())
{
parameter=null;
parameterName=requestParameterService.getName();
parameterType=requestParameterService.getParameterType();
parameter=request.getParameter(parameterName);
parameterTypeName=parameterType.toString();
if(requestParameterService.getIsJson())
{
Gson gson=new Gson();
BufferedReader bufferedReader=request.getReader();
StringBuffer stringBuffer=new StringBuffer();
String b;
String rawString;
while(true)
{
b=bufferedReader.readLine();
if(b==null) break;
stringBuffer.append(b);
}
rawString=stringBuffer.toString();
System.out.println(rawString);
requestParameters[i]=gson.fromJson(rawString,parameterType);
}

else if(requestParameterService.getIsApplicationScope())
{
requestParameters[i]=new ApplicationScope(getServletContext());
}
else if(requestParameterService.getIsApplicationDirectory())
{
requestParameters[i]=new ApplicationDirectory(new File(servletContext.getRealPath("")));
}
else if(requestParameterService.getIsSessionScope())
{
requestParameters[i]=new SessionScope(request.getSession());
}
else if(requestParameterService.getIsRequestScope())
{
requestParameters[i]=new RequestScope(request);
}

else if(requestParameterService.getIsPrimitive())
{
if(parameterTypeName.equals("int") || parameterTypeName.equals("Integer"))
{
try
{
requestParameters[i]=Integer.parseInt(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("long") || parameterTypeName.equals("Long"))
{
try
{
requestParameters[i]=Long.parseLong(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("short") || parameterTypeName.equals("Short"))
{
try
{
requestParameters[i]=Short.parseShort(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("double") || parameterTypeName.equals("Double"))
{
try
{
requestParameters[i]=Double.parseDouble(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("boolean")  || parameterTypeName.equals("Boolean"))
{
requestParameters[i]=Boolean.parseBoolean(parameter);
}
else if(parameterTypeName.equals("float") || parameterTypeName.equals("Float"))
{
try
{
requestParameters[i]=Float.parseFloat(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("byte") || parameterTypeName.equals("Byte"))
{
try
{
requestParameters[i]=Byte.parseByte(parameter);
}catch(NumberFormatException nef)
{
System.out.println(nef);
response.sendError(HttpServletResponse.	SC_METHOD_NOT_ALLOWED);
return;
}
}
else if(parameterTypeName.equals("char") || parameterTypeName.equals("Character"))
{
requestParameters[i]=parameter.charAt(0);
}
else
{
requestParameters[i]=parameter;
}
}
i++;
}//parameter loop ends


//setting parameters ends
//calling method

		System.out.println("Request parameter size"+requestParameters.length);
		for(int j=0;j<requestParameters.length;j++)
		{
			System.out.println("__________----"+requestParameters[j]);
		}	

//case of forwarding
ServiceResponse serviceResponse=new ServiceResponse();
serviceResponse.setIsSuccess(true);
if(requestParameters.length!=0)
{
serviceResponse.setResult(method.invoke(classObject,requestParameters));
}
else
{
serviceResponse.setResult(method.invoke(classObject));
}
pw.println(new Gson().toJson(serviceResponse));
pw.flush();
}catch(Exception e)
{
System.out.println(e+" "+request.getRequestURI());
System.out.println(e.getCause());
ServiceResponse serviceResponse=new ServiceResponse();
serviceResponse.setIsSuccess(false);
serviceResponse.setException(e.getCause().toString());
pw.println(new Gson().toJson(serviceResponse));
pw.flush();
}


}//function ends

}//class ends