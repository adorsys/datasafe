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
    -H 'Content-Type: application/json;charset=UTF-8' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTA1LCJyb2wiOlsiUk9MRV9VU0VSIl19.7pOIzuFJbffo3w6f5O423ECMsaif-IrVN5h4Mc0PxqwG16XBnYW_fDxI83cg64GLeRPB_aJyLdk0sEFv0RRuZg' \
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
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTA2LCJyb2wiOlsiUk9MRV9VU0VSIl19.JDAtiTfSoxnuKQmEZZchF4_g-hWjj19rJ__4cX5rX07XQn4XHVxDvYMi5I88dfmdPorXJU_KOlWz9JnM6qen4g' \
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
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTE1LCJyb2wiOlsiUk9MRV9VU0VSIl19.pJG4DCMc8PBuxZp3Mn7SyvlbSRpZqqpymuKm_5CaDJai8bIKrHMVYZFl1k-4pGxMmNLQUALqqp4ZYQYX8gKEog' \
    -H 'password: test' \
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
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTE2LCJyb2wiOlsiUk9MRV9VU0VSIl19.UR26MJ3IYnm0SbtVeyIxWeoSsGO10tYxEJfNQnmH5zn4Ve9O6YE3oiD1Jk7c0dlv4kvOMPjpGU6Duouj-DT9JA'
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
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTE1LCJyb2wiOlsiUk9MRV9VU0VSIl19.pJG4DCMc8PBuxZp3Mn7SyvlbSRpZqqpymuKm_5CaDJai8bIKrHMVYZFl1k-4pGxMmNLQUALqqp4ZYQYX8gKEog' \
    -H 'Content-Type: application/octet-stream;charset=UTF-8' \
    -H 'password: test' \
    -H 'user: test' \
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
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTE2LCJyb2wiOlsiUk9MRV9VU0VSIl19.UR26MJ3IYnm0SbtVeyIxWeoSsGO10tYxEJfNQnmH5zn4Ve9O6YE3oiD1Jk7c0dlv4kvOMPjpGU6Duouj-DT9JA'
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
    -H 'user: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTIwLCJyb2wiOlsiUk9MRV9VU0VSIl19.DLtGjyz8jPfsAfWiBj9kXvTIOZE-9xFGBFY-K6fAWkeg3b5J-0rhcOkbrMdIOLJsyB5goTve1HAm2nxb1cIasA'
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
    -H 'user: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTIwLCJyb2wiOlsiUk9MRV9VU0VSIl19.DLtGjyz8jPfsAfWiBj9kXvTIOZE-9xFGBFY-K6fAWkeg3b5J-0rhcOkbrMdIOLJsyB5goTve1HAm2nxb1cIasA'
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
    -H 'users: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTIwLCJyb2wiOlsiUk9MRV9VU0VSIl19.DLtGjyz8jPfsAfWiBj9kXvTIOZE-9xFGBFY-K6fAWkeg3b5J-0rhcOkbrMdIOLJsyB5goTve1HAm2nxb1cIasA' \
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
    -H 'user: test' \
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTIwLCJyb2wiOlsiUk9MRV9VU0VSIl19.DLtGjyz8jPfsAfWiBj9kXvTIOZE-9xFGBFY-K6fAWkeg3b5J-0rhcOkbrMdIOLJsyB5goTve1HAm2nxb1cIasA'
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
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTA5LCJyb2wiOlsiUk9MRV9VU0VSIl19.40Bnuv3ciee27Z92ErTCL_gMYy18Kuj42Xw3PCg1kifiUCSvq8Sz-hlJ8bThBB1EBWXowC58KbROaaoJBdZPlw' \
    -H 'password: test' \
    -H 'user: test' \
    -H 'Content-Type: application/json;charset=UTF-8'
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
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTA5LCJyb2wiOlsiUk9MRV9VU0VSIl19.40Bnuv3ciee27Z92ErTCL_gMYy18Kuj42Xw3PCg1kifiUCSvq8Sz-hlJ8bThBB1EBWXowC58KbROaaoJBdZPlw' \
    -H 'password: test' \
    -H 'Accept: application/octet-stream' \
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
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTA5LCJyb2wiOlsiUk9MRV9VU0VSIl19.40Bnuv3ciee27Z92ErTCL_gMYy18Kuj42Xw3PCg1kifiUCSvq8Sz-hlJ8bThBB1EBWXowC58KbROaaoJBdZPlw' \
    -H 'Content-Type: application/octet-stream;charset=UTF-8' \
    -H 'password: test' \
    -H 'user: test' \
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
    -H 'token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzZWN1cmUtYXBpIiwiYXVkIjoic2VjdXJlLWFwcCIsInN1YiI6InVzZXJuYW1lIiwiZXhwIjoxNTYzNzIwOTA5LCJyb2wiOlsiUk9MRV9VU0VSIl19.40Bnuv3ciee27Z92ErTCL_gMYy18Kuj42Xw3PCg1kifiUCSvq8Sz-hlJ8bThBB1EBWXowC58KbROaaoJBdZPlw' \
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
