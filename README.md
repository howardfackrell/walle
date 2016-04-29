#Walle

Walle is a humble robot and titular character in a pixar film. Now he is also a java agent who hopes to be 
the defender of truth and justice on your development machine, 'Walling' off environments  so that you don't 
acccidentall modify the qa database when you think you are running against dev, or vise versa
 

## How Do I use Walle?
Clone this repo and run a 'mvn install'. Then add this VM option to any java program that you want Walle to 
keep an eye on:
```
-javaagent:/path/to/jar/walle-1.0-SNAPSHOT.jar
```

## What does Walle do?
Walle will stop your program before it starts if the environment for any of the LOCATION env variable doesn't match whats expected 
 for the environment specified in your config jar. Walle will tell you which variables have unexpected values in them.
 Everything Walle says is prefixed with the string 'WALL-E:'

### Options
 You can add a single string to the end of the java agent parameter. It looks like this:
 ```
    -javaagent:/path/to/jar/walle-1.0-SNAPSHOT.jar=verbosewarn
 ```
 Walle currently supports these options:
  - warn: don't exit the program, just print the mismatched environment variables, if any, and go on
  - verbose: print all environment variables, not just the ones that appear to be mismatched
 
## Why implement Walle as a Java Agent?

### Pros
  - You don't have to modify your code at all to use him
  - Walle runs before any of your code, so he can stop things before any harm is done

### Cons
  - You have to remember to enable the Walle Agent or he does nothing
 
## Why do I have to build Walle myself?
If Walle proves useful, we'll give him a better place to live. Otherwise he's going to the scrap pile.

## What if I hate Walle?
Make a pull request, or propose an alternative solution


