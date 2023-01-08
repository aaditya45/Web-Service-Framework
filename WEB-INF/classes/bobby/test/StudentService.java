package bobby.test;
import com.my.pack.webService.annotations.*;
import com.my.pack.webService.pojo.*;

@SecuredAccess(checkPost="bobby.test.StudentHome",guard="access")
@InjectSessionScope
@Get
@Path("/StudentService")
public class StudentService 
{
private SessionScope sessionScope;
@AutoWired(name="nnumberrr")
private Integer number;
@AutoWired(name="myyNamee")
private String myName;
@AutoWired(name="Stud")
private Student s;

public void sets(Student s)
{
this.s=s;
System.out.println("s works .............");
}

public void setnumber(Integer number)
{
this.number=number;
System.out.println("number works .............");
}

public void setmyName(String myName)
{
this.myName=myName;
System.out.println("myName works .............");
}




public void setSessionScope(SessionScope sessionScope)
{
System.out.println("session scope created");
this.sessionScope=sessionScope;
}

@Post
@Path("/addStudent")
public void addStudent(Student s,ApplicationScope as)
{
System.out.println("RollNUmber 	: "+s.getRollNumber());
System.out.println("Name  : "+s.getName());
System.out.println("Age	: "+s.getAge());
System.out.println(sessionScope);
System.out.println("Student added");
}

public void removeStudent(Student student)
{
System.out.println("Student removed");
}

@Path("/editStudent")
public void editStudent(@RequestParameter("rollNumber1")int rollNumber1,@RequestParameter("rollNumber2")int rollNumber2,ApplicationScope as,ApplicationDirectory ad)
{
System.out.println(rollNumber1);
System.out.println(rollNumber2);
System.out.println("Student edited");
}

@ForwardTo("/Teacher")
@Path("/hideStudent")
public void hideStudent(@RequestParameter("rollNumber1")byte rollNumber,@RequestParameter("rollNumber2")String name)
{
System.out.println(rollNumber);
System.out.println(name);
System.out.println("Student hided");
}


}//class ends 