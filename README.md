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

- create a database with any name for now springsecurity 
- create user table with id, active, password, roles, username

Create HomeResource with following RestController

```java
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

Create SecurityConfiguration class that will extends WebSecurityConfigurerAdapter and override configure method which will take argument as HttpSecurity for Authorization with following code to allow certain urls based on Role and annotation this class with @EnableWebSecurity.

```java
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

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

Now we have the authorization setup, it's time to setup the authentication.

#### How do you setup authentication for spring security to connect using JPA to MySQL

There are a bunch of out of the box implementation for authentication that spring security provides, For example if we want to use JDBC authentication, there is a JDBC authentication manager that we can use and tell what the database is and what is query and all other stuffs. 

However for JPA there is no out of the box implementation for Authentication. 

> AuthenticationManager authenticate() talks to AuthenticationProvider authenticate() and that calls UserDetailsService loadUserByUsername(). 

Spring security has a way for you to provide UserDetailsService. You can create UserDetailsService and give it to spring by taking username and by returning the UserObject of type UserDetails. Spring security manage the rest.

In order to have spring security work with JPA whats you need to do is create an instance of this UserDetailsService.

Let's create a hardcoded authentication first, It does not matter UserDetailsService is getting data using JPA or from text file.

> We can make use of userDetailsService() on AuthenticationManager and this method let's us pass user instance, @Autowired the UserDetailsService interface and pass that reference to userDetailsService() like following and create an implementation by creating a new class and make that as Service by annotating with @Service

```java
@Autowired
UserDetailsService userDetailsService;

@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	auth.userDetailsService(userDetailsService);
}
```

- Create an implementation of UserDetailsService by creating a class MyUserDetailsService that implements UserDetailsService.
- Override the unimplemented method loadUserByUsername(String username) that will return UserDetails.

MyUserDetailsService.java

```java
@Service
public class MyUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new MyUserDetails(username);
    }
}
```

- Create a class MyUserDetails that will be implementation of UserDetails interface
- Provide implementation for the un-implemented method by overiding them
> Spring security takes that those value that we will return from implemented class

```java
public class MyUserDetails implements UserDetails {

    private String username;

    public MyUserDetails(String username){
        this.username = username;
    }

    public MyUserDetails(){
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return "pass";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

```

- Create a password encoder bean in SecurityConfiguration class by using NoOpPasswordEncoder to use plain text as password 

```java
@Bean
public PasswordEncoder getPasswordEncoder(){
	return NoOpPasswordEncoder.getInstance();
}
```

Run the application by commenting jpa and mysql dependency from the pom.xml and it should work fine. Like that we can return user from any source it can be jpa or text file 

Now let's use JPA and find data from database.

- Create a model class User

```java
@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String username;
    private String password;
    private boolean active;
    private String roles;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
```

MyUserDetails.java

```java
public class MyUserDetails implements UserDetails {

    private String username;
    private String password;
    private boolean active;
    private List<GrantedAuthority> authorities;

    public MyUserDetails(User user){
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.active = user.isActive();
        this.authorities = Arrays.stream(user.getRoles().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public MyUserDetails(){
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
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

MyUserDetailsService.java

```java
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        user.orElseThrow(()-> new UsernameNotFoundException("Not found : "+username));
        return user.map(MyUserDetails::new).get();
    }
}
```

UserRepository.java

```java
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
```

application.properties

```
spring.datasource.url=jdbc:mysql://localhost:3306/springsecuritydemo
spring.datasource.username=root
spring.datasource.password=vikas
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming-strategy=org.hibernate.cfg.ImprovedNamingStrategy
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```


## Spring Boot and Spring Security auth with LDAP

##### What is LDAP

- Lightweight Directory Access Protocol
- The Lightweight Directory Access Protocol is an open, vendor-neutral, industry standard application protocol for accessing and maintaining distributed directory information services over an Internet Protocol (IP) network. Directory services play an important role in developing intranet and Internet applications by allowing the sharing of information about users, systems, networks, services, and applications throughout the network. As examples, directory services may provide any organized set of records, often with a hierarchical structure, such as a corporate email directory. Similarly, a telephone directory is a list of subscribers with an address and a phone number.

- Lightweight directory access protocol (LDAP) is a protocol that makes it possible for applications to query user information rapidly.

- Someone within your office wants to do two things: Send an email to a recent hire and print a copy of that conversation on a new printer. LDAP (lightweight directory access protocol) makes both of those steps possible.

- Set it up properly, and that employee doesn't need to talk with IT to complete the tasks.

> Companies store usernames, passwords, email addresses, printer connections, and other static data within directories. LDAP is an open, vendor-neutral application protocol for accessing and maintaining that data. LDAP can also tackle authentication, so users can sign on just once and access many different files on the server.

> LDAP is a protocol, so it doesn't specify how directory programs work. Instead, it's a form of language that allows users to find the information they need very quickly.


#### Spring boot + Spring Security + LDAP setup

- Create a new spring boot project with following dependency 
	- Spring Web
	- Spring Security
	
##### Setup LDAP server

- open pom.xml and following dependency

```
<dependency>
	<groupId>com.unboundid</groupId>
	<artifactId>unboundid-ldapsdk</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.ldap</groupId>
	<artifactId>spring-ldap-core</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-ldap</artifactId>
</dependency>
```
- above dependency ensure to local instance of ldap up and running
- add user in local ldap instance

application.properties

```
spring.ldap.embedded.port=8389 #local instance of ldap will run on this port
```

- The second property is to the reference to the file which contains the seeded data 

```
spring.ldap.embedded.ldif=classpath:ldap-data.ldif
```

- Create a new ldif file in resource directory

> LDIF: LDAP dtata interchange format

- Get user data configuration sample from spring documentation guide or copy below code snippet and paste it to ldif file https://spring.io/guides/gs/authenticating-ldap/

```
dn: dc=springframework,dc=org
objectclass: top
objectclass: domain
objectclass: extensibleObject
dc: springframework

dn: ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: groups

dn: ou=subgroups,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: subgroups

dn: ou=people,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: people

dn: ou=space cadets,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: space cadets

dn: ou=\"quoted people\",dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: "quoted people"

dn: ou=otherpeople,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: otherpeople

dn: uid=ben,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Ben Alex
sn: Alex
uid: ben
userPassword: $2a$10$c6bSeWPhg06xB1lvmaWNNe4NROmZiSpYhlocU/98HNr2MhIOiSt36

dn: uid=bob,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Bob Hamilton
sn: Hamilton
uid: bob
userPassword: bobspassword

dn: uid=joe,ou=otherpeople,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Joe Smeth
sn: Smeth
uid: joe
userPassword: joespassword

dn: cn=mouse\, jerry,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Mouse, Jerry
sn: Mouse
uid: jerry
userPassword: jerryspassword

dn: cn=slash/guy,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: slash/guy
sn: Slash
uid: slashguy
userPassword: slashguyspassword

dn: cn=quote\"guy,ou=\"quoted people\",dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: quote\"guy
sn: Quote
uid: quoteguy
userPassword: quoteguyspassword

dn: uid=space cadet,ou=space cadets,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Space Cadet
sn: Cadet
uid: space cadet
userPassword: spacecadetspassword



dn: cn=developers,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfUniqueNames
cn: developers
ou: developer
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
uniqueMember: uid=bob,ou=people,dc=springframework,dc=org

dn: cn=managers,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfUniqueNames
cn: managers
ou: manager
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
uniqueMember: cn=mouse\, jerry,ou=people,dc=springframework,dc=org

dn: cn=submanagers,ou=subgroups,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfUniqueNames
cn: submanagers
ou: submanager
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
```


- we can add next properties to specify base org from ldif file 

```
spring.ldap.embedded.base-dn=dc=springframework,dc=org
```

- Create a HomeResource 

```java
@RestController
public class HomeResource {

    @GetMapping("/")
    public String index(){
        return "Home page";
    }
    
}
```

- Create SecurityConfiguration 

```java
package com.example.springsecurityldap;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.ldapAuthentication()
                .userDnPatterns("uid={0},ou=people")
                .groupSearchBase("ou=groups")
                .contextSource()
                .url("ldap://localhost:8389/dc=springframework,dc=org")
                .and()
                .passwordCompare()
                .passwordEncoder(new BCryptPasswordEncoder())
                .passwordAttribute("userPassword");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest()
                .fullyAuthenticated()
                .and()
                .formLogin();
    }


}
```

- Now we can start our application and make use of ldif configured username `ben` and password `benspassword`.


