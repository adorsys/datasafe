# 1. SimpleDatasafeAdapter

The simple datasafe adapter is an easy to use wrapper about the basic functionality of the main service. This service is called the SimpleDatasafeService.
Its interface looks very similar to the DocumentSafeService of the business-layer of the previous project docusafe (https://github.com/adorsys/docusafe).

The SimpleDatasafeService is currenty reduced to the main functionalities:

- create/destroy/check existance of user
- create/delete/list files

As this project must not have any dependencies to the docusafe-project all used parameter classes like documentFQN, UserID etc. exist in this project as copy and thus have different package names.

```
# service
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;

# parameter classes of service methods
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentDirectoryFQN;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.simple.adapter.api.types.ListRecursiveFlag;
```

## 1.1 usage:

The recommended way to get an instance is the injection with spring-boot. 

### 1.1.1 direct injection

For that the annotation
```
@UseDatasafeSpringConfiguration
```
has to be used, then the service directly can be injected
```
       @Autowired
       private SimpleDatasafeService service;
```
The default root bucket of the autowired SimpleDatasafeService is read from the system settings. They can be defined by a yml, system properties or environment variables, like it is the default behaviour of spring. The yml for an amazons3 dfs will look like:
```
datasafe:
  storeconnection:
    amazons3:
      url: https://s3.amazonaws.com
      accesskey: *
      secretkey: *
      region: eu-central-1
      rootbucket: adorsys-docusafe      
      nohttps: (optional, default false - use https to reach s3 endpoint)
      threadpoolsize: (optional, default 5, how many workers should send chunk requests)
      maxconnections: (optional, if unset default of amazon is taken)
      requesttimeout: (optional, if unset default of amazon is taken)
      
```
or for a filesystem like that:
```
datasafe:
  storeconnection:
    filesystem:
      rootbucket: target/datasafe-files
```

### 1.1.2 factory injection
Rather than autowiring the DataSafeService directly, it can be created with an autowired SpringSimpleDatasafeServiceFactory.
The factory has a method, to get a SimpleDatasafeService with another rootBucket, or to be more precise, with a deeper bucket which is below the default root bucket.
So accessing a SimpleDatasafeService with
```
       @Autowired
       private SpringSimpleDatasafeServiceFactory factory;
       
       ...
       
       
       SimpleDatasafeService service = factory.getSimpleDataSafeServiceWithSubdir("/deeper/and/deeper");
```
and a default root bucket target/datasafe-file would return a SimpleDatasafeService with the root bucket
```
    target/datasafe-file/deeper/and/deeper
```

### 1.1.3 alternative
Another - but not recommended - way to retrieve an instance of the SimpleDatasafeService is the direct instantiation with
```
new SimpleDatasafeServiceImp()
```
In this case, the DFS can be defined by the two system properites:
```
-DSC-AMAZONS3=<url>,<simpleAccessKey>,<simpleSecretKey>,<region>,<root bucket>
-DSC-FILESYSTEM=<root bucket>
```

## 1.2 features
The default behavior of the datasafe service is to encrypt all data and to encrypt the path of the documents too. For testing  - and for testing only - theses features can be switched off.
So the following properties *never must be used in production*:
```
SC-NO-BUCKETPATH-ENCRYPTION 
SC-NO-CMSENCRYPTION-AT-ALL
```
So running the app with <code>-DSC-NO-BUCKETPATH-ENCRYPTION</code> (or <code>-DSC-NO-BUCKETPATH-ENCRYPTION=true</code>) would avoid any path encryption. Ommitting the property or setting it to false would run with the default behaviour.
Similar running the app with <code>-DSC-NO-CMSENCRYPTION-AT-ALL</code> (or <code>-DSC-NO-CMSENCRYPTION-AT-ALL=true</code>) would suppress any content encryption.


## 1.3 pom

### 1.3.1 spring
If spring is used the only module to be included now is  
```
        <dependency>
            <groupId>de.adorsys</groupId>
            <artifactId>datasafe-simple-adapter-spring</artifactId>
            <version>${project.version}</version>
        </dependency>
```

### 1.3.2 direct instantiation
If spring is not used the only module to be included now is  
```
        <dependency>
            <groupId>de.adorsys</groupId>
            <artifactId>datasafe-simple-adapter-impl</artifactId>
            <version>${project.version}</version>
        </dependency>
```

### 1.3.3 known dependency collisions
If you are using datasafe with the local filesystem as the dfs and you get a 
<code>ClassNotFoundException</code> for <code>
com.google.common.io.MoreFiles
</code> it is highly propable that you have an old guava dependecy. You should exclude this dependency so that the new guava version can be included from the datasafe.jar.
