package com.my.pack.webService;
import com.my.pack.webService.annotations.*;
import com.my.pack.webService.pojo.*;
import com.my.pack.webService.model.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.annotation.*;
import com.my.pack.webService.annotations.*;

public class MYWebServiceStartup extends HttpServlet
{
private WebServiceModel webServiceModel;
private LinkedList<String> classesName; 
private LinkedList<Service> onStartupMethods;
private static final Set<String> IS_PRIMITIVE = getIsPrimitiveOrWrapper();
private static Set<String> getIsPrimitiveOrWrapper()
{
Set<String> ret = new HashSet<String>();
ret.add("class java.lang.Integer");
ret.add("class java.lang.Character");
ret.add("class java.lang.Byte");
ret.add("class java.lang.Short");
ret.add("class java.lang.Boolean");
ret.add("class java.lang.Long");
ret.add("class java.lang.Float");
ret.add("class java.lang.Double");
ret.add("class java.lang.Void");
ret.add("int");
ret.add("char");
ret.add("byte");
ret.add("short");
ret.add("boolean");
ret.add("long");
ret.add("float");
ret.add("double");
ret.add("void");
return ret;
}


public void init()
{
ServletConfig servletConfig=getServletConfig();
String servicePackagePrefix=servletConfig.getInitParameter("SERVICE_PACKAGE_PREFIX");
String pathToUserPackagePrefix=getServletContext().getRealPath("/WEB-INF/classes/")+servicePackagePrefix;
System.out.println(pathToUserPackagePrefix);
getListOfclasses(pathToUserPackagePrefix,servicePackagePrefix);
	for(String s : classesName)
	{
	System.out.println(s);
	}
populateWebServiceModel();
//adding webServiceModel in application/Scope
ServletContext servletContext=getServletContext();
servletContext.setAttribute("webServiceModel",webServiceModel);

	Map<String,Service> map=webServiceModel.getServiceMap();
	for(Map.Entry<String,Service> entry : map.entrySet())
	{
	Service s=entry.getValue();
	System.out.println(s.getPath());
	System.out.println(s.getServiceClass());
	System.out.println(s.getServiceMethod());
	System.out.println("get : "+s.getIsGetAllowed());
	System.out.println("post : "+s.getIsPostAllowed());
	System.out.println(s.getForwardTo());	
	System.out.println("InjectSessionScope : "+s.getInjectSessionScope());
	System.out.println("InjectApplicationScope : "+s.getInjectApplicationScope());
	System.out.println("InjectRequestScope : "+s.getInjectRequestScope());
	System.out.println("InjectApplicationDirectory : "+s.getInjectApplicationDirectory());
	System.out.println("auto wired list size  "+s.getAutoWiredList().size());
	System.out.println("request parameters  "+s.getRequestParameterList().size());
	System.out.println("request parameters  "+s.getIsSecured());
	System.out.println("request parameters  "+s.getCheckPost());
	System.out.println("request parameters  "+s.getGuard());
		
	System.out.println("*********");
	}
populateOnStartupMethods();
executeOnStartupMethods();
System.out.println("???????"+onStartupMethods.size()+"?????????");
for(Service s : onStartupMethods)
{
System.out.println(s.getPriority());
}

}//init function ends

private void getListOfclasses(String path,String prefixName)
{
this.classesName=new LinkedList<String>();
final File folder = new File(path);
Queue<File> Q=new LinkedList<File>();
File f;
for (final File fileEntry : folder.listFiles()) 
{
if (fileEntry.isDirectory()) 
{
Q.add(fileEntry);
}
else 
{
if(fileEntry.getName().endsWith(".class")) 
{
String packName,rawPath;
rawPath=fileEntry.getPath();
packName=rawPath.substring(rawPath.indexOf(prefixName+"\\")+6,rawPath.indexOf(".class"));
packName=packName.replace("\\",".");
packName=prefixName+"."+packName;
this.classesName.add(packName);
}
}
}

while(!Q.isEmpty())
{
f=Q.peek(); Q.poll();
for (final File fileEntry : f.listFiles()) 
{
if (fileEntry.isDirectory()) 
{
Q.add(fileEntry);
}
else 
{
if(fileEntry.getName().endsWith(".class")) 
{
String packName,rawPath;
rawPath=fileEntry.getPath();
packName=rawPath.substring(rawPath.indexOf(prefixName+"\\")+6,rawPath.indexOf(".class"));
packName=packName.replace("\\",".");
packName=prefixName+"."+packName;
this.classesName.add(packName);
}
}
}
}
}//function ends

private void populateWebServiceModel()
{
this.webServiceModel=new WebServiceModel();
try
{
for(String className : classesName)
{
Method methods[];
Path classAnnotation,methodAnnotation;
Class clazz=Class.forName(className);
classAnnotation=(Path)clazz.getAnnotation(Path.class);
if(classAnnotation!=null)
{
String pathKey="";
boolean isGetAllowed=false;
boolean isPostAllowed=false;
boolean injectSessionScope=false;
boolean injectApplicationScope=false;
boolean injectRequestScope=false;
boolean injectApplicationDirectory=false;
SecuredAccess securedAccessAnnotationOnClass;
SecuredAccess securedAccessAnnotationOnMethod;

//checking for get and post method validation
if(clazz.getAnnotation(Post.class)!=null) isPostAllowed=true;
if(clazz.getAnnotation(Get.class)!=null) isGetAllowed=true;
//checking for inject
if(clazz.getAnnotation(InjectSessionScope.class)!=null) injectSessionScope=true;
if(clazz.getAnnotation(InjectApplicationScope.class)!=null) injectApplicationScope=true;
if(clazz.getAnnotation(InjectRequestScope.class)!=null) injectRequestScope=true;
if(clazz.getAnnotation(InjectApplicationDirectory.class)!=null) injectApplicationDirectory=true;

//setting up autoWiredService starts here
LinkedList<AutoWiredService> ListOfAutoWiredServices=new LinkedList<AutoWiredService>();
Field fields[];
fields=clazz.getDeclaredFields();
AutoWired autoWiredAnnotation;
for(Field field : fields)
{
autoWiredAnnotation=(AutoWired)field.getAnnotation(AutoWired.class);
if(autoWiredAnnotation!=null)
{
AutoWiredService autoWiredService=new AutoWiredService();
autoWiredService.setName(autoWiredAnnotation.name());
autoWiredService.setAutoWiredField(field);
ListOfAutoWiredServices.add(autoWiredService);
}
}//fields loop  ends

//setting up auto WiredService ends here

pathKey=classAnnotation.value();
methods=clazz.getDeclaredMethods();
for(Method method : methods)
{
int jsonCount=0;
int scopeAndDirectoryCount=0;
int requestParameterCount=0;
pathKey=classAnnotation.value();
methodAnnotation=(Path)method.getAnnotation(Path.class);
if(methodAnnotation!=null)
{
ForwardTo forwardToAnnotation; 
pathKey=pathKey+methodAnnotation.value();
	System.out.println(pathKey);

//populating requestParameterList of service Starts 
Parameter parameters[];
parameters=method.getParameters();
LinkedList<RequestParameterService> requestParameterList=new LinkedList<RequestParameterService>();

for(Parameter parameter : parameters)
{
RequestParameter requestParameterAnnotation;
requestParameterAnnotation=parameter.getAnnotation(RequestParameter.class);

if(requestParameterAnnotation!=null)
{
RequestParameterService requestParameterService=new RequestParameterService();
requestParameterService.setName(requestParameterAnnotation.value());
requestParameterService.setParameterType(parameter.getType());
System.out.println("pppppppp type"+parameter.getType());
System.out.println("FFFFFFFFFFFFF premitive "+parameter.getType().isPrimitive());
System.out.println("FFFFFFFFFFFFF to string "+parameter.getType().toString());
//System.out.println("FFFFFFFFFFFFF to string "+IS_PRIMITIVE.contains(parameter.getType().toString()));
if(IS_PRIMITIVE.contains(parameter.getType().toString()) || parameter.getType().toString().equals("class java.lang.String"))
{
requestParameterService.setIsPrimitive(true);
requestParameterList.add(requestParameterService);
requestParameterCount++;
}
continue;
}

if(parameter.getType().toString().equals("class com.my.pack.webService.pojo.ApplicationScope"))
{
RequestParameterService requestParameterService=new RequestParameterService();
requestParameterService.setParameterType(parameter.getType());
requestParameterService.setIsApplicationScope(true);
requestParameterList.add(requestParameterService);
scopeAndDirectoryCount++;
continue;
}
if(parameter.getType().toString().equals("class com.my.pack.webService.pojo.ApplicationDirectory"))
{
RequestParameterService requestParameterService=new RequestParameterService();
requestParameterService.setParameterType(parameter.getType());
requestParameterService.setIsApplicationDirectory(true);
requestParameterList.add(requestParameterService);
scopeAndDirectoryCount++;
continue;
}
if(parameter.getType().toString().equals("class com.my.pack.webService.pojo.SessionScope"))
{
RequestParameterService requestParameterService=new RequestParameterService();
requestParameterService.setParameterType(parameter.getType());
requestParameterService.setIsSessionScope(true);
requestParameterList.add(requestParameterService);
scopeAndDirectoryCount++;
continue;
}
if(parameter.getType().toString().equals("class com.my.pack.webService.pojo.RequestScope"))
{
RequestParameterService requestParameterService=new RequestParameterService();
requestParameterService.setParameterType(parameter.getType());
requestParameterService.setIsRequestScope(true);
requestParameterList.add(requestParameterService);
scopeAndDirectoryCount++;
continue;
}


if(!parameter.getType().isPrimitive() && !parameter.getType().toString().equals("class java.lang.String"))
{
RequestParameterService requestParameterService=new RequestParameterService();
requestParameterService.setParameterType(parameter.getType());
requestParameterService.setIsJson(true);
requestParameterList.add(requestParameterService);
jsonCount++;
continue;
}
}//parameter loop ends

//populating requestParameterList of service ends 

Service service=new Service();
service.setPath(pathKey);
service.setServiceClass(clazz);
service.setServiceMethod(method);
if(isGetAllowed==false)
{
if(method.getAnnotation(Get.class)!=null) service.setIsGetAllowed(true);
else service.setIsGetAllowed(false);
}
else
{
service.setIsGetAllowed(true);
}
if(isPostAllowed==false)
{
if(method.getAnnotation(Post.class)!=null) service.setIsPostAllowed(true);
else service.setIsPostAllowed(false);
}
else
{
service.setIsPostAllowed(true);
}
/* if user not specifying for post and get type in class or methods
 so, we have to provide access for both get and post */
if(service.getIsPostAllowed()==false && service.getIsGetAllowed()==false)
{
service.setIsPostAllowed(true);
service.setIsGetAllowed(true);
}

forwardToAnnotation=(ForwardTo)method.getAnnotation(ForwardTo.class);
if(forwardToAnnotation!=null)
{
service.setForwardTo(forwardToAnnotation.value());
}

service.setInjectSessionScope(injectSessionScope);
service.setInjectApplicationScope(injectApplicationScope);
service.setInjectRequestScope(injectRequestScope);
service.setInjectApplicationDirectory(injectApplicationDirectory);

service.setAutoWiredList(ListOfAutoWiredServices);
service.setRequestParameterList(requestParameterList);

//security part starts 
securedAccessAnnotationOnClass=(SecuredAccess)clazz.getAnnotation(SecuredAccess.class);
if(securedAccessAnnotationOnClass!=null)
{
service.setIsSecured(true);
service.setCheckPost(securedAccessAnnotationOnClass.checkPost());
service.setGuard(securedAccessAnnotationOnClass.guard());
}
securedAccessAnnotationOnMethod=(SecuredAccess)clazz.getAnnotation(SecuredAccess.class);
if(securedAccessAnnotationOnMethod!=null)
{
service.setIsSecured(true);
service.setCheckPost(securedAccessAnnotationOnMethod.checkPost());
service.setGuard(securedAccessAnnotationOnMethod.guard());
}

//security part ends

if(jsonCount<=1 && (jsonCount+requestParameterCount+scopeAndDirectoryCount)==parameters.length)
{
this.webServiceModel.addInMap(pathKey,service);
}
}
}//method loop ends
}

}//class loop ends

}catch(Exception e)
{
System.out.println(e);
}
}//function ends


private void populateOnStartupMethods()
{
this.onStartupMethods=new LinkedList<Service>();
try
{
for(String className : classesName)
{
Method methods[];
Path classAnnotation,methodAnnotation;
OnStartup onStartupAnnotation;
Class clazz=Class.forName(className);
methods=clazz.getDeclaredMethods();
for(Method method : methods)
{
onStartupAnnotation=(OnStartup)method.getAnnotation(OnStartup.class);
if(onStartupAnnotation!=null && onStartupAnnotation.priority()>0)
{
Service service=new Service();
service.setServiceClass(clazz);
service.setServiceMethod(method);
service.setRunOnStart(true);
service.setPriority(onStartupAnnotation.priority());
this.onStartupMethods.add(service);
}
}//method loop end
}//class loop ends
//sorting onStartupMethods on base of priority 
Collections.sort(this.onStartupMethods,new Comparator<Service>(){
public int compare(Service s1,Service s2) 
{
return s1.getPriority() - s2.getPriority();
}
});
}catch(Exception e)
{
System.out.println(e);
}
}//function ends


private void executeOnStartupMethods()
{
try
{
for(Service service : this.onStartupMethods)
{
Class clazz=service.getServiceClass();
Method method=service.getServiceMethod();
Object O=clazz.newInstance();
method.invoke(O);
}
}catch(Exception e)
{
System.out.println(e);
}
}//function ends



}//class ends