import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.annotation.*;
import com.my.pack.webService.annotations.*;

class psp
{
private static List<String> classNames;
private static String prefixName;
public static void main(String gg[])
{
final File folder = new File("C:/tomcat9/webapps/mywebservice/WEB-INF/classes/bobby");
classNames=new LinkedList<String>();
prefixName="bobby";
listFilesForFolder(folder);
getDetailsOfClass(classNames);
}

public static void listFilesForFolder(final File folder) 
{
Queue<File> Q;
Q=new LinkedList<File>();
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
classNames.add(packName);
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
classNames.add(packName);
}
}
}
}
}//function ends

public static void getDetailsOfClass(List<String> classNames)
{
try
{
for(String className : classNames)
{
String pathKey="";
Path classA,methodA;  //annotation path

Class c=Class.forName(className);
classA=(Path)c.getAnnotation(Path.class);
if(classA!=null) 
{
System.out.println(classA.value());
Method methods[];
methods=c.getDeclaredMethods();
for(Method m : methods)
{
methodA=(Path)m.getAnnotation(Path.class);
if(methodA!=null)System.out.println(methodA.value());
}
}
}
}catch(Exception e)
{
System.out.println(e);
}
}//function ends

}//class ends