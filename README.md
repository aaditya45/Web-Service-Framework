# Web-Service-Framework

Servlet programming can be very hectic sometimes. Web-service framework saves developers from writing an absurd amount of code and saves time. Instead of writing servlets users can do they can use annotations for different purposes. For example, to accept a get request from a client, the user can use the "@Get" annotation on the service method along with the "@Path" annotation on the controller class to handle the request.
### Annotations Provided by the framework
<ul>
  <li>@Path("/pathPattern") applicable for both class as well as method level</li>
<li>@AutoWired(name="propname") this annotation will help when user wants to inject data in any property from request or session or servletContext(Property level)</li>
<li>@Forward(urlPattern) this annotation will forward request to url pattern(Method level)</li>
<li>@OnStartup(priority) the method annotated with this annotation will invoked according to priority number</li>
<li>@Get() to specify a service is of get type (class and both method level the method or class which is not annotated with this annotation will be application for both get and post efaultly)</li>
<li>@Post() to specify a service is of post type (class and both method level the method or class which is not annotated with this annotation will be application for both get and post efaultly)</li>
<li>@RequestParameter(name="paramname") this annotation is used to get data coming from query string in get request directly inside a method parameter</li>
<li>@InjectRequestParameter("paramname") this annotation is used to wrap data coming from query string to a corresponding property specified</li>
<li>@PathVariable() this annotation will help to get data coming along with request uri inside a method parameter</li>
<li>@InjectApplicationDirectory this annotation is used to get wrapper of ApplicationDirectory class inside a property specified</li>
<li>@InjectApplicationScope this annotation is used to inject application scope(ServletContext) wrapper inside a service as a Field</li>
<li>@InjectSessionScope this annotation is used to inject Session scope(HttpSession) wrapper inside a service as a Field</li>
<li>@InjectRequestScope this annotation is used to inject Request scope(HttpServletRequest) wrapper inside a service as a Field</li>
</ul>
