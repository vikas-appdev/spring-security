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

----------------------------------------------------------------------------------------------------

## How does spring security authentication work.

Servlet FIlters intercept all request and maps it to Spring security own filter called DelegatingFilterProxy. If you are not working in spring boot app you need to manuallly add this filter to intercept all the request. Delegating filters catch all filters that spring security add as a starting point and delegate it to other spring security specific filters to do different things depends on the urls being requested or the configuration going on for spring security app. 

After providing credentials as input it returns Principal(Information about the logged in user) on successl authentication. When spring security performs Authentication it keeps tracks of both input and output using an object of type Authentication. 

Authentication is a internal spring security interface, and authentication object ment to hold credentials before authentication once user is authenticated it holds the principal. You can think of authentication as DTO(Data transfer object) for authentication befor authentication and holder of Principal after authentication. 

##### What's the thing that does the actual authentication

There are several ways in which it can be done in spring security application. But the most common pattern you will find is using providers. 

There is something called AuthenticationProvider. It is responsible for doing the actual authentication. This is an interface that have a method called authenticate() and you need to have implementation in your application and tells spring security about it, then spring security will calls this method to authenticate user. 

------------------------------------------------------------------------------------------------------

## How to do JDBC Authentication with Spring Security

- Create a new project using start.spring.io
- Add following dependency 
	- spring web
	- spring security
	- h2 database
	- jdbc api

RestController

```java
package com.example.springsecurityjdbc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeResource {
    @GetMapping("/")
    public String home(){
        return ("<h1>Welcome</h1>");
    }

    @GetMapping("/user")
    public String user(){
        return ("<h1>Welcome User</h1>");
    }

    @GetMapping("/admin")
    public String admin(){
        return ("<h1>Welcome Admin</h1>");
    }
}
```

SecurityConfiguration

```java
package com.example.springsecurityjdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .withDefaultSchema()
                .withUser(
                        User.withUsername("user")
                                .password("pass")
                                .roles("USER")
                ).withUser(
                        User.withUsername("admin")
                                .password("pass")
                                .roles("ADMIN")
                );
    }

    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin").hasRole("ADMIN")
                .antMatchers("/user").hasAnyRole("USER", "ADMIN")
                .antMatchers("/").permitAll()
                .and().formLogin();
    }
}
```

Above block of code will create default schema and add two user with given username and password in H2 database.

We can enable h2 console by adding following application properties and see the generated schema

```
spring.h2.console.enabled=true
```

This will enable the h2 console at /h2-console

By default, Spring Boot configures the application to connect to an in-memory store with the username sa and an empty password.
However, we can change those parameters by adding the following properties to the application.properties file:

```
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```


> We will modify this to get data from existing db table

```java
@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource);
                
    }
```

> create schema.sql and data.sql in resource directory to create and add data in h2 CLick Here https://docs.spring.io/spring-security/reference/6.0.0-M5/servlet/appendix/database-schema.html#page-title

schema.sql

```sql
create table users(
	username varchar_ignorecase(50) not null primary key,
	password varchar_ignorecase(50) not null,
	enabled boolean not null
);

create table authorities (
	username varchar_ignorecase(50) not null,
	authority varchar_ignorecase(50) not null,
	constraint fk_authorities_users foreign key(username) references users(username)
);
create unique index ix_auth_username on authorities (username,authority);
```

data.sql

```sql
INSERT into users(username, password, enabled) VALUES ('user', 'pass', true);
INSERT into users(username, password, enabled) VALUES ('admin', 'pass', true);

INSERT into authorities(username, authority) VALUES('user', 'ROLE_USER');
INSERT into authorities(username, authority) VALUES('admin', 'ROLE_ADMIN');
```

It will take data from datastore by looking into default table 

If you have different table then you can pass query to query different table along with data source.


```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	auth.jdbcAuthentication()
			.dataSource(dataSource)
			.usersByUsernameQuery("select username, password, enabled from users where username = ?")
			.authoritiesByUsernameQuery("select username, authority from authorities where username = ?");
}
```



## How to do JPA Authentication with Spring Security and MySQL

