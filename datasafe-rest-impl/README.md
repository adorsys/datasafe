# Overview

Datasafe REST Application exists for demonstration purposes of datasafe
library. It consists endpoints for working with users, private
documents, private documents with versioning support, public
documents(documents exchange) and authentication.

# Authentication API

# User API

## Objects

| Code     | Description |
| -------- | ----------- |
| Username | username.   |
| Password | password.   |

UserDTO

## Methods

### Create user

This method allows to create new datasafe user. Http methods of request
is PUT.

#### Request header

| Name    | Description                             |
| ------- | --------------------------------------- |
| `token` | Bearer authentication token is required |

#### Request fields

| Path       | Type     | Description            |
| ---------- | -------- | ---------------------- |
| `userName` | `String` | Name of user to create |
| `password` | `String` | Password of user       |

#### Example request

``` bash
$ curl 'http://example.com/datasafe/user' -i -X PUT \
    -H 'Accept: application/json' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5Nzg5LCJyb2wiOlsiUk9MRV9VU0VSIl19.4nk7AL_YcoDdOYvwNuQ6i95XbTrVGpaKxYwVeFO27Jt0PDxy9e4K4s68D_LtLf6MHC6TETko8jsh-mMiOFsBKw' \
    -H 'Content-Type: application/json;charset=UTF-8' \
    -d '{"userName":"testUser","password":"testPassword"}'
```

#### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

### /user

This method deletes user and all his files from datasafe storage. Http
method of request is DELETE.

#### Example request

``` bash
$ curl 'http://example.com/datasafe/user' -i -X DELETE \
    -H 'Accept: application/json' \
    -H 'password: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5Nzg5LCJyb2wiOlsiUk9MRV9VU0VSIl19.4nk7AL_YcoDdOYvwNuQ6i95XbTrVGpaKxYwVeFO27Jt0PDxy9e4K4s68D_LtLf6MHC6TETko8jsh-mMiOFsBKw' \
    -H 'user: test' \
    -H 'Content-Type: application/json;charset=UTF-8'
```

#### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

# Private Document API

## List Documents

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Example request

``` bash
$ curl 'http://example.com/datasafe/documents/' -i -X GET \
    -H 'password: test' \
    -H 'user: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5Nzk4LCJyb2wiOlsiUk9MRV9VU0VSIl19.UlPlQ5cDfK5oMkTvkjO555QbJwXPj58cDRRJNtByP1mQOCXEdcC3oiq_et_rPegD1l1_fsI4EDdkBQFceaxh3g'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
Content-Length: 21
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Content-Type: application/json;charset=UTF-8
Cache-Control: no-cache, no-store, max-age=0, must-revalidate

["/path/to/file.txt"]
```

#### Response body

    ["/path/to/file.txt"]

## Read Document

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Path fields

| Parameter | Description      |
| --------- | ---------------- |
| `path`    | path to the file |

/document/{path}

### Example request

``` bash
$ curl 'http://example.com/datasafe/document/path/to/file' -i -X GET \
    -H 'password: test' \
    -H 'Accept: application/octet-stream' \
    -H 'user: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5Nzk4LCJyb2wiOlsiUk9MRV9VU0VSIl19.UlPlQ5cDfK5oMkTvkjO555QbJwXPj58cDRRJNtByP1mQOCXEdcC3oiq_et_rPegD1l1_fsI4EDdkBQFceaxh3g'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Content-Length: 12

file content
```

#### Response body

    file content

## Write Document

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Path fields

| Parameter | Description      |
| --------- | ---------------- |
| `path`    | path to the file |

/document/{path}

### Example request

``` bash
$ curl 'http://example.com/datasafe/document/path/to/file' -i -X PUT \
    -H 'Content-Type: application/octet-stream;charset=UTF-8' \
    -H 'password: test' \
    -H 'user: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5Nzk4LCJyb2wiOlsiUk9MRV9VU0VSIl19.UlPlQ5cDfK5oMkTvkjO555QbJwXPj58cDRRJNtByP1mQOCXEdcC3oiq_et_rPegD1l1_fsI4EDdkBQFceaxh3g' \
    -d 'file content'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

## Remove Document

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Path fields

| Parameter | Description      |
| --------- | ---------------- |
| `path`    | path to the file |

/document/{path}

### Example request

``` bash
$ curl 'http://example.com/datasafe/document/path/to/file' -i -X DELETE \
    -H 'password: test' \
    -H 'user: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5Nzk4LCJyb2wiOlsiUk9MRV9VU0VSIl19.UlPlQ5cDfK5oMkTvkjO555QbJwXPj58cDRRJNtByP1mQOCXEdcC3oiq_et_rPegD1l1_fsI4EDdkBQFceaxh3g'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

# Inbox API

## List Inbox

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Example request

``` bash
$ curl 'http://example.com/datasafe/inbox/' -i -X GET \
    -H 'password: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5ODAxLCJyb2wiOlsiUk9MRV9VU0VSIl19.iUuT7Mg_2QoTQQ0l9MC0a_h3EAIfpAEqp3d3nYGSOnaK22FV7Wpo22OcKZSF4w6hWAThRQ0l_83TDDEjoWj9Xg' \
    -H 'user: test'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
Content-Length: 21
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Content-Type: application/json;charset=UTF-8
Cache-Control: no-cache, no-store, max-age=0, must-revalidate

["/path/to/file.txt"]
```

#### Response body

    ["/path/to/file.txt"]

## Read from Inbox

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Path fields

| Parameter | Description      |
| --------- | ---------------- |
| `path`    | path to the file |

/inbox/{path}

### Example request

``` bash
$ curl 'http://example.com/datasafe/inbox/test.txt' -i -X GET \
    -H 'password: test' \
    -H 'Accept: application/octet-stream' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5ODAxLCJyb2wiOlsiUk9MRV9VU0VSIl19.iUuT7Mg_2QoTQQ0l9MC0a_h3EAIfpAEqp3d3nYGSOnaK22FV7Wpo22OcKZSF4w6hWAThRQ0l_83TDDEjoWj9Xg' \
    -H 'user: test'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Content-Length: 5
Cache-Control: no-cache, no-store, max-age=0, must-revalidate

hello
```

#### Response body

    hello

## Write to Inbox

### Request header

| Name    | Description                             |
| ------- | --------------------------------------- |
| `token` | Bearer authentication token is required |
| `users` | recipients array                        |

### Path fields

| Parameter | Description      |
| --------- | ---------------- |
| `path`    | path to the file |

/inbox/{path}

### Example request

``` bash
$ curl 'http://example.com/datasafe/inbox/test.txt' -i -X PUT \
    -H 'Content-Type: application/octet-stream;charset=UTF-8' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5ODAxLCJyb2wiOlsiUk9MRV9VU0VSIl19.iUuT7Mg_2QoTQQ0l9MC0a_h3EAIfpAEqp3d3nYGSOnaK22FV7Wpo22OcKZSF4w6hWAThRQ0l_83TDDEjoWj9Xg' \
    -H 'users: test' \
    -d 'file content'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

## Remove from Inbox

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Path fields

| Parameter | Description      |
| --------- | ---------------- |
| `path`    | path to the file |

/inbox/{path}

### Example request

``` bash
$ curl 'http://example.com/datasafe/inbox/test.txt' -i -X DELETE \
    -H 'password: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5ODAxLCJyb2wiOlsiUk9MRV9VU0VSIl19.iUuT7Mg_2QoTQQ0l9MC0a_h3EAIfpAEqp3d3nYGSOnaK22FV7Wpo22OcKZSF4w6hWAThRQ0l_83TDDEjoWj9Xg' \
    -H 'user: test'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

# Versioned Document API

## List Documents

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Example request

``` bash
$ curl 'http://example.com/datasafe/versioned/' -i -X GET \
    -H 'password: test' \
    -H 'user: test' \
    -H 'Content-Type: application/json;charset=UTF-8' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5NzkyLCJyb2wiOlsiUk9MRV9VU0VSIl19.RfHmjCxU8EHvFlsrHfT5jFFyWMeLdhii4f4JQqZfDPU6bK6eEMrXljplb_2HwSp9MwSOCSaN-5dgFUnTwDz7VA'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
Content-Length: 21
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Content-Type: application/json;charset=UTF-8
Cache-Control: no-cache, no-store, max-age=0, must-revalidate

["/path/to/file.txt"]
```

#### Response body

    ["/path/to/file.txt"]

## Read Document

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Path fields

| Parameter | Description      |
| --------- | ---------------- |
| `path`    | path to the file |

/versioned/{path}

### Example request

``` bash
$ curl 'http://example.com/datasafe/versioned/path/to/file' -i -X GET \
    -H 'password: test' \
    -H 'Accept: application/octet-stream' \
    -H 'user: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5NzkyLCJyb2wiOlsiUk9MRV9VU0VSIl19.RfHmjCxU8EHvFlsrHfT5jFFyWMeLdhii4f4JQqZfDPU6bK6eEMrXljplb_2HwSp9MwSOCSaN-5dgFUnTwDz7VA'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Content-Length: 5
Cache-Control: no-cache, no-store, max-age=0, must-revalidate

hello
```

#### Response body

    hello

## Write Document

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Path fields

| Parameter | Description      |
| --------- | ---------------- |
| `path`    | path to the file |

/versioned/{path}

### Example request

``` bash
$ curl 'http://example.com/datasafe/versioned/path/to/file' -i -X PUT \
    -H 'Content-Type: application/octet-stream;charset=UTF-8' \
    -H 'password: test' \
    -H 'user: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5NzkyLCJyb2wiOlsiUk9MRV9VU0VSIl19.RfHmjCxU8EHvFlsrHfT5jFFyWMeLdhii4f4JQqZfDPU6bK6eEMrXljplb_2HwSp9MwSOCSaN-5dgFUnTwDz7VA' \
    -d 'file content'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

## Remove Document

### Request header

| Name       | Description                             |
| ---------- | --------------------------------------- |
| `token`    | Bearer authentication token is required |
| `user`     | datasafe username                       |
| `password` | datasafe user’s password                |

### Path fields

| Parameter | Description      |
| --------- | ---------------- |
| `path`    | path to the file |

/versioned/{path}

### Example request

``` bash
$ curl 'http://example.com/datasafe/versioned/path/to/file' -i -X DELETE \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzk5NzkzLCJyb2wiOlsiUk9MRV9VU0VSIl19.850IpK6hwtpDh-sH5rMxcYHa5fRb2X9yulDdbvHsEmA7a9JSfZWzrFVVAHjcLILymRSLNn7LTH7XWaaGt4PcnA' \
    -H 'password: test' \
    -H 'user: test'
```

### Example success response

``` http
HTTP/1.1 200 OK
Pragma: no-cache
X-XSS-Protection: 1; mode=block
Expires: 0
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```
