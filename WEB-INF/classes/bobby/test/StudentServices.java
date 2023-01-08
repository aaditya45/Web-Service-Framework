package bobby.test;
import com.my.pack.webService.annotations.*;
import com.my.pack.webService.pojo.*;
import java.sql.*;
import java.util.*;
@Path("/sstudentServices")
public class StudentServices
{
public static Connection getConnection()
{
Connection connection=null;
try
{
Class.forName("com.mysql.cj.jdbc.Driver");
connection=DriverManager.getConnection("jdbc:mysql://localhost:3306/hrdb","hr","hr");
}catch(Exception e)
{
System.out.println(e);
}
return connection;
}//function ends

@Path("/aaddStudent")
@Post
public void addStudent(Student s) throws Exception
{
Connection connection=getConnection();
ResultSet resultSet;
PreparedStatement preparedStatement;
preparedStatement=connection.prepareStatement("select rollNumber from Student where rollNumber=?");
preparedStatement.setInt(1,s.getRollNumber());
resultSet=preparedStatement.executeQuery();
if(resultSet.next())
{
resultSet.close();
preparedStatement.close();
connection.close();
throw new Exception("student with roll number "+s.getRollNumber()+" already exists.");
}
preparedStatement=connection.prepareStatement("insert into Student (rollNumber,name,age) values(?,?,?)");
preparedStatement.setInt(1,s.getRollNumber());
preparedStatement.setString(2,s.getName());
preparedStatement.setInt(3,s.getAge());
preparedStatement.executeUpdate();
preparedStatement.close();
connection.close();
}//function ends

@Post
@Path("/uupdateStudent")
public void updateStudent(Student s) throws Exception
{
Connection connection=getConnection();
ResultSet resultSet;
PreparedStatement preparedStatement;
preparedStatement=connection.prepareStatement("select rollNumber from Student where rollNumber=?");
preparedStatement.setInt(1,s.getRollNumber());
resultSet=preparedStatement.executeQuery();
if(!resultSet.next())
{
resultSet.close();
preparedStatement.close();
connection.close();
throw new Exception("student with roll number "+s.getRollNumber()+" not exists.");
}
preparedStatement=connection.prepareStatement("update Student set name=? , age=? where rollNumber=?");
preparedStatement.setString(1,s.getName());
preparedStatement.setInt(2,s.getAge());
preparedStatement.setInt(3,s.getRollNumber());

preparedStatement.executeUpdate();
preparedStatement.close();
connection.close();
}

@Get
@Path("/rremoveStudent")
public void removeStudent(@RequestParameter("rollNumber") int rollNumber) throws Exception
{
Connection connection=getConnection();
ResultSet resultSet;
PreparedStatement preparedStatement;
preparedStatement=connection.prepareStatement("select rollNumber from Student where rollNumber=?");
preparedStatement.setInt(1,rollNumber);
resultSet=preparedStatement.executeQuery();
if(!resultSet.next())
{
resultSet.close();
preparedStatement.close();
connection.close();
throw new Exception("student with roll number "+rollNumber+" not exists.");
}
preparedStatement=connection.prepareStatement("delete from Student where rollNumber=?");
preparedStatement.setInt(1,rollNumber);
preparedStatement.executeUpdate();
preparedStatement.close();
connection.close();
}

@Path("/getAllStudent")
public List<Student> getAllStudent() throws Exception
{
List<Student> list=new LinkedList<Student>();
Student s;
Connection connection=getConnection();
ResultSet resultSet;
PreparedStatement preparedStatement;
preparedStatement=connection.prepareStatement("select * from Student");
resultSet=preparedStatement.executeQuery();
while(resultSet.next())
{
s=new Student();
s.setRollNumber(resultSet.getInt("rollNumber"));
s.setName(resultSet.getString("name"));
s.setAge(resultSet.getInt("age"));
list.add(s);
}
resultSet.close();
preparedStatement.close();
connection.close();
return list;
}

@Path("/getStudentByRollNumber")
@Get
public Student getStudentByRollNumber(@RequestParameter("rollNumber") int rollNumber) throws Exception
{
Student s=new Student();
Connection connection=getConnection();
ResultSet resultSet;
PreparedStatement preparedStatement;
preparedStatement=connection.prepareStatement("select * from Student where rollNumber=?");
preparedStatement.setInt(1,rollNumber);
resultSet=preparedStatement.executeQuery();
if(!resultSet.next())
{
resultSet.close();
preparedStatement.close();
connection.close();
throw new Exception("student with roll number "+rollNumber+" not exists.");
}
s.setRollNumber(rollNumber);
s.setName(resultSet.getString("name"));
s.setAge(resultSet.getInt("age"));
resultSet.close();
preparedStatement.close();
connection.close();
return s;
}//function ends

}//class ends