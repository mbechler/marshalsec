# Java Unmarshaller Security - Turning your data into code execution

If you came here for Log4Shell/CVE-2021-44228, you may want to read about 
the exploitation vectors and affected Java runtime versions:
<https://mbechler.github.io/2021/12/10/PSA_Log4Shell_JNDI_Injection/>

## Paper

It's been more than two years since Chris Frohoff and Garbriel Lawrence have presented their research into Java object deserialization vulnerabilities ultimately resulting in what can be readily described as the biggest wave of remote code execution bugs in Java history.

Research into that matter indicated that these vulnerabilities are not exclusive to mechanisms as expressive as Java serialization or XStream, but some could possibly be applied to other mechanisms as well.

This paper presents an analysis, including exploitation details, of various Java open-source marshalling libraries that allow(ed) for unmarshalling of arbitrary, attacker supplied, types and shows that no matter how this process is performed and what implicit constraints are in place it is prone to similar exploitation techniques.

Full paper is at [marshalsec.pdf](https://www.github.com/mbechler/marshalsec/blob/master/marshalsec.pdf?raw=true)

## Disclaimer

All information and code is provided solely for educational purposes and/or testing your own systems for these vulnerabilities.

## Usage

Java 8 required. Build using maven ```mvn clean package -DskipTests```. Run as

```shell
java -cp target/marshalsec-[VERSION]-SNAPSHOT-all.jar marshalsec.<Marshaller> [-a] [-v] [-t] [<gadget_type> [<arguments...>]]
```

where

* **-a** - generates/tests all payloads for that marshaller
* **-t** - runs in test mode, unmarshalling the generated payloads after generating them.
* **-v** - verbose mode, e.g. also shows the generated payload in test mode.
* **gadget_type** - Identifier of a specific gadget, if left out will display the available ones for that specific marshaller.
* **arguments** - Gadget specific arguments

Payload generators for the following marshallers are included:<br />

| Marshaller                      | Gadget Impact
| ------------------------------- | ----------------------------------------------
| BlazeDSAMF(0&#124;3&#124;X)     | JDK only escalation to Java serialization<br/>various third party libraries RCEs
| Hessian&#124;Burlap             | various third party RCEs
| Castor                          | dependency library RCE
| Jackson                         | **possible JDK only RCE**, various third party RCEs
| Java                            | yet another third party RCE
| JsonIO                          | **JDK only RCE**
| JYAML                           | **JDK only RCE**
| Kryo                            | third party RCEs
| KryoAltStrategy                 | **JDK only RCE**
| Red5AMF(0&#124;3)               | **JDK only RCE**
| SnakeYAML                       | **JDK only RCEs**
| XStream                         | **JDK only RCEs**
| YAMLBeans                       | third party RCE

## Arguments and additional prerequisites

### System Command Execution

* **cmd** - command to execute
* **args...** - additional parameters passed as arguments

No prerequisites.

### Remote Classloading (plain)

* **codebase** - URL to remote codebase
* **class** - Class to load

**Prerequisites**:

* Set up a webserver hosting a Java classpath under some path.
* Compiled class files to load need to be served according to Java classpath conventions.

### Remote Classloading (ServiceLoader)

* **service_codebase** - URL to remote codebase

The service to load is currently hardcoded to *javax.script.ScriptEngineFactory*.

**Prerequisites**:

* Same as plain remote classloading.
* Also needs a provider-configuration file at *<codebase>*/META-INF/javax.script.ScriptEngineFactory
  containing the targeted class name in plain text.
* Target class specified there needs to implement the service interface *javax.script.ScriptEngineFactory*.


### JNDI Reference indirection

* **jndiUrl** - JNDI URL to trigger lookup on


**Prerequisites**:

* Set up a remote codebase, same as remote classloading.
* Run a JNDI reference redirector service pointing to that codebase -
  two implementations are included: *marshalsec.jndi.LDAPRefServer* and *RMIRefServer*.
  ```shell
  java -cp target/marshalsec-[VERSION]-SNAPSHOT-all.jar marshalsec.jndi.(LDAP|RMI)RefServer <codebase>#<class> [<port>]
  ```
* Use (ldap|rmi)://*host*:*port*/obj as the *jndiUrl*, pointing to that service's listening address.

## Running tests

There are a couple of system properties that control the arguments when running tests (through maven or when using **-a**)

* **exploit.codebase**, defaults to *http://localhost:8080/*
* **exploit.codebaseClass**, defaults to *Exploit*
* **exploit.jndiUrl**, defaults to *ldap://localhost:1389/obj*
* **exploit.exec**, defaults to */usr/bin/gedit*

Tests run with a SecurityManager installed that checks for system command execution as well as code executing from remote codebases.
For that to work the loaded class in use must trigger some security manager check.



