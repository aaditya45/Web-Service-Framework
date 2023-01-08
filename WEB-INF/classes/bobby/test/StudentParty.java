package bobby.test;
import com.my.pack.webService.annotations.*;

@Path("/StudentParty")
public class StudentParty 
{
@Path("/apniParty")
public void ourParty()
{
}

@Path("/tumhariParty")
@ForwardTo("/tum")
public void yourParty()
{
}

@Post
@Path("/sabkiParty")
@ForwardTo("/ham")
public void everyOneParty()
{
}


}//class ends 