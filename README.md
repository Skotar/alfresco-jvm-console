# Alfresco JVM Console

The purpose of this project is to execute Java and Kotlin code on Alfresco Content Services. It should be used only in development/prototyping stage. 

The project consists of two parts: Alfresco Content Services platform module and IntelliJ plugin.

## What does this project offer?

* Support for Java and Kotlin (Kotlin libraries must be present on Alfresco Content Services)
* Support for Spring dependency injection annotations (@Autowired, @Qualifier, @Value)
* Debugger support
* Code is compiled by IntelliJ and bytecode is sent to a server (it is executed directly from IntelliJ by the plugin)
* Compiled code may be added:
    * As a new ClassLoader (a child of the main ClassLoader) - a reference to a new ClassLoader is lost after code execution
    * To the main ClassLoader - loaded classes aren't removed after code execution and they can be used anywhere

## Quick presentation
[![Click to Watch!](https://i.imgur.com/EVR1Nj7.png)](https://vimeo.com/381796634 "Click to watch!")

## Requirements
* The file must be in a module
* The file must have a package name
* The file must contain a class
* The class must contain a function that name starts with **alfresco**

See **example** folder for more details (it contains Java and Kotlin examples)

## Setup
You can use files from **Releases** or build it yourself
### Alfresco Content Services module
The module is based on SDK 4.0.0 so you can build the module and run Alfresco Content Services using
```
./run.sh build_start
```
from **alfresco** folder.

### IntelliJ plugin
Run
```
./gradlew runIde
```
from **intellij** folder.
