Shibboleth Authentication Plugin for Liferay
============================================

This plugin is an extended version of the plugin by rsheshi@gmail.com (see below).

Additional features has been added:

* auto create users using Shibboleth attributes (email, first name, last name)
* auto update user information upon login
* added options to specify the headers (Shibboleth attributes) to be used for extracting email, first name and last name
* basic attribute to role mapping

Requirements
------------

* Apache 2.x with mod_ssl and mod_proxy_ajp
* Shibboleth SP 2.x

Introduction
------------

Currently, there is no native Java Shibboleth service provider. If you need to protect your Java web
with Shibboleth, you have to run Apache with mod_shib in front of your servlet container (Tomcat, JBoss, ...).
The protected application must not be accessible directly, it must be run on a private address. Apache will intercept
requests, and after performing all authentication related tasks, it will pass the request to the backend servlet
container using AJP (Apache JServ Protocol).

Shibboleth Service Provider
---------------------------

A standard Shibboleth Service Provider instance may be used with one difference - the attribute preffix must bes
set to "AJP_", otherwise user attributes from Shibboleh will not be accessible in the application.

    <ApplicationDefaults entityID="https://liferay-test/shibboleth"
        REMOTE_USER="uid eppn persistent-id targeted-id" 
        attributePrefix="AJP_">


Apache configuration
--------------------

First, we need to set the AJP communication with the backend in our virtual host configuration:

    ProxyPass / ajp://localhost:8009/
    ProxyPassReverse / ajp://localhost:8009/
    
Then we'll configure Shibboleth to be "activated" for the whole site:

    <Location />
        AuthType shibboleth
        require shibboleth
    </Location>
    
And require a Shibboleth session at the "login" location:

    <Location /c/portal/login>
        AuthType shibboleth
        ShibRequireSession On
        require valid-user
    </Location>


Container's AJP connector
---------------------

Make sure, the backend servlet container has properly configured AJP connector. For example, in JBoss it is not enabled by default and you have to explicitly enable it:

    # cd $JBOSS_HOME/bin
    # ./jboss-cli.sh 
    You are disconnected at the moment. Type 'connect' to connect to the server or 'help' for the list of supported commands.
    [disconnected /] connect
    [standalone@localhost:9999 /] /subsystem=web:read-children-names(child-type=connector)
    {
        "outcome" => "success",
        "result" => ["http"]
    }
    [standalone@localhost:9999 /] /subsystem=web/connector=ajp:add(socket-binding=ajp, protocol="AJP/1.3", enabled=true, scheme=ajp)
    {"outcome" => "success"}
    [standalone@localhost:9999 /] /subsystem=web:read-children-names(child-type=connector)
    {
        "outcome" => "success",
        "result" => [
            "ajp",
            "http" 
        ]
    }


Plugin installation and configuration
-------------------------------------

Clone the repository and run the Maven install script:

    # git clone https://github.com/ivan-novakov/liferay-shibboleth-plugin.git
    # cd liferay-shibboleth-plugin
    # mvn install

Then deploy the WAR file to your servlet container.
After a successful installation a new "Shibboleth" section appears in the Liferay's Control panel at "Portal Settings / Authentication". You can adjust Shibboleth authentication there. The most important setting is the name of the attribute from with the user identity is taken.
At the same time, in "Portal Settings --> Authentication --> General" you must set "How do users authenticate?" to "By Screen Name" and disable all "Allow..." options.

Further steps
-------------

Logging can be enabled at "Control panel --> Server Administration --> Log Levels" by adding these categories:

    com.liferay.portal.security.auth.ShibbolethAutoLogin
    com.liferay.portal.servlet.filters.sso.shibboleth.ShibbolethFilter
    
These settings will work untill server reboot only. To make them permanent you need to create a special configuration file placed at `$JBOSS_HOME/standalone/deployments/ROOT.war/WEB-INF/classes/META-INF/portal-log4j-ext.xml`:

    <?xml version="1.0"?>
    <!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
    
    <log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">
    
        <appender name="CONSOLE" class="org.apache.log4j.ConsoleAppender">
            <layout class="org.apache.log4j.PatternLayout">
                <param name="ConversionPattern" value="%d{ABSOLUTE} %-5p [%c{1}:%L] %m%n" />
            </layout>
        </appender>
    
        <category name="com.liferay.portal.security.auth.ShibbolethAutoLogin">
            <priority value="INFO" />
        </category>
    
        <category name="com.liferay.portal.servlet.filters.sso.shibboleth.ShibbolethFilter">
            <priority value="INFO" />
        </category>
    
    </log4j:configuration>



Licence
-------

[MIT Licence](http://opensource.org/licenses/mit-license.php)


Contact
-------

* homepage: https://github.com/ivan-novakov/liferay-shibboleth-plugin


Original plugin
---------------

By rsheshi@gmail.com:

http://code.google.com/p/liferay-shibboleth-plugin/
