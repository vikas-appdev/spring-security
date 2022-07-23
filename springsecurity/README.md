## What is spring security?

## Why Spring security?

#### Application security framework
- Login and logout functionality
- Allow/Blocks access to URLs to logged in users
- Allow/block access to URLs to logged in users and with certain roles
  **Flexible and customizable**

#### Handles common vulnerablities like
- Session fixation
- Clickjacking
- Click site request forgery


#### Widely Adopted
- Popular target for hacker :(
- Vulnerablities get the most attention and quick response



#### What Spring Security can do

- Username / password authentication
- SSO / Okta / LDAP
- App level authorization
- Intra app authorization using technology like OAuth
- Microservices security (using tokens, JWT)
- Method level security

--------------------------------------------------------------------

## 5 core concepts in spring security

- Authentication
- Authorization
- Principal
- Granted Authority
- Roles

#### Authentication : It answer the question who are you

- Knowledge Based Authentication : Password, Pincode, Answer to secret or personal question
  - Easy to Implement and use / Simple
  - Disadvantage : If someone find the password behave like you

- Possession based Authentication :
  - Phone or text message
  - Key cards and badges
  - Access token device


> Knowledge Based Authentication + Possession based Authentication =  Multifactor Authentication


#### Authorization : Examine what you want and allowed to do that or not. Can this user do this?


#### Principal :
- Principal is the person you have identified through the process of authentication.
- Currently logged in user


#### Authority:
How does Authorization happen?

- Based on role certain access is allowed
- Bunch of permission that are allowed for a given user
- In spring security this set of permission is called Granted Authority

Example:
A store clerk can do
- do_checkout
- make_store_announcement

Department Manager
- do_checkout
- make_store_announcement
- view_department_financial
- view_department_inventory


Store Manager
- do_checkout
- make_store_announcement
- view_department_financial
- view_department_inventory
- view_store_financials


Authorities are fine-grained

In this case someone has to assigne all the authorities for every user that can be tedious, This is where you create the concept of Role


Role:
- Group of Authority are usally assigned together

Example

- role_store_clerk
  - do_checkout
  - make_store_announcement


- role_department_manager
  - do_checkout
  - make_store_announcement
  - view_department_financial
  - view_department_inventory


- role_store_manager
  - do_checkout
  - make_store_announcement
  - view_department_financial
  - view_department_inventory
  - view_store_financials


Assign the role and autority will be automitacally will be assigned

Roles are coarse-grained permission unlike fine-grained permission that authorities have.


-----------------------------------------------------------------


## Adding spring security to new spring boot project

- In spring boot starter web project just add simple dependency for `spring-boot-starter-security` this will add all required dependency

- Adding this dependency into classpath will immeditely start working

- Using Filters(Servlet) it add login page on all routes


### Spring Security Default Behaviour

- Adds mandatory authentication for URLs
- /error page will be ignored by spring security
- Adds login form
- Handles login error
- Creates a user and sets a default password

> Spring security generates a new password each time application restart with a user "user"

- You can customized created default user and password from `application.properties` file by adding following properties

```
spring.security.user.name=foo
spring.security.user.password=bar
```

------------------------------------------

## How to configure authentication in spring security

#### AuthenticationManager
AuthenticationManager has authenticate() method that returns successful authentication or throws exception on authentication failure

> We don't work directly with AuthenticationManager, we configure it using builder pattern using 'AuthenticationManagerBuilder' to configure what the Authentication should actually do.

Steps
- Get hold of AuthenticationManagerBuilder
- Set the configuration on it

SecurityConfiguration.java

```java
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("blah")
                .password("blah")
                .roles("USER")
                .and()
                .withUser("foo")
                .password("foo")
                .roles("ADMIN");
    }

    
}
```

HomeResource.java

```java
@RestController
public class HomeResource {

    @GetMapping("/")
    public String home(){
        return ("<h1>Welcome</h1>");
    }
}
```




#### How to set a password encoder?

Just expose an @Bean of type PasswordEncoder!

- Add Bean method inside SecurityConfiguration.java

```java
@Bean
public PasswordEncoder getPasswordEncoder(){
	return NoOpPasswordEncoder.getInstance();
}
```

----------------------------------------------------------------------------------

## How to configure Authorization in spring boot

> Using HttpSecurity we can configure Authorization

We need to override configure method from WebSecurityConfigurerAdapter and pass the URLs with antmatcher like following

SecurityConfiguration.java
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
	http.authorizeRequests()
			.antMatchers("/admin").hasRole("ADMIN")
			.antMatchers("/user").hasAnyRole("USER", "ADMIN")
			.antMatchers("/").permitAll()
			.and().formLogin();
}
```

In above code hasRole will allow specific role user to add multiple role use user we can make use of hasAnyRole. We do role mapping in heirarchy and in last we do wild cart mapping like /** to allow all with permitAll() method.

## How does spring security authentication work.

