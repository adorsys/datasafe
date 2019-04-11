# CodingRules

## ExceptionHandling
- We do as less ExceptionHandling as necessary. This means, we never catch Exceptions for logging or converting them.
- We never use checked Exceptions at all.
- If we have code that may throw an checked exception, we annotate our method with @SneakyThrows
- We are allowed to throw our own exceptiopns. They are allway derived by the de.adorsys.common.exceptions.BaseException of the de.adorsys.common.basetypes. 

## Entities

- Each Entity is its own class. We do not use strings or integers in our interfaces. For example a name and a password both are strings in the end. But we have a class UserName and another class Password. Just to make clear what is what. This is an important feature for typesafety.
