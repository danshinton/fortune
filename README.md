# Fortune API

## About

This project is just a simple containerized microservice with a small footprint
written as an example for my personal portfolio. It provides a RESTful API that
manages a list of fortune cookie quotes.

Since this is a portfolio project you may find the documentation and comments to
be verbose. This is because I want to convey my line of thinking in the code. You
may also find the solution to be overkill. This is because this project can be
used as a base project for highly performant microservices.

### How It Works

#### Server

When the server starts, it connects to a [SQLite](https://www.sqlite.org/) 
database containing fortunes. If the database does not exist, it uses 
[Flyway](https://flywaydb.org/) to create a new one. The server gathers 
configuration from the environment using the [Owner](http://owner.aeonbits.org/)
library and then initializes a [Javalin](https://javalin.io/) web server to 
handle requests.

Serialization and deserialization of JSON requests and responses is done using
[Jackson](https://github.com/FasterXML/jackson). For calls that require 
authentication, [JWT](https://jwt.io/) bearer tokens passed via the 
`Authentication` header are required for access. The response objects use the 
same common structure to make response parsing simpler.

Database access is managed via an accessor pattern using [JDBI](https://jdbi.org/) 
with [HikariCP](https://github.com/brettwooldridge/HikariCP) for connection pooling.

#### Client

There isn't anything special about the RESTful service that would require a
specialized client. Calls can be made using command line tools such as 
[cURL](https://curl.se/). However, since this is an example project, why not 
write a client library to make integration easier?

An interface to the Fortune API is provided using [OkHttp](https://square.github.io/okhttp/)
and [Retrofit](https://square.github.io/retrofit/). It can be accessed as a Java 
library or via a command line generated with [Picocli](https://picocli.info/). 

#### Other Design Considerations

The use of the [Immutables](https://immutables.github.io/) library is employed 
to provide a builder pattern for configuration and domain objects. Unit tests 
use [JUnit](https://junit.org/junit5/) and [Mockito](https://site.mockito.org/) 
to exercise the codebase. There is an integration test in the client module 
which uses the server code to stand up a local server on a random port with an 
in-memory database to make calls against.

Speaking of modules, the project is broken up into three modules. One for the
microservice server, one for the client library, and one for any common objects
shared between the two.

Code is documented using Javadoc and this README file. 

## API

The following RESTful calls are available:

| Path                | Type  | Auth | Description          |
|---------------------|-------|------|----------------------|
| /api/v1/fortune     | GET   | No   | Get a random fortune |
| /api/v1/fortune     | POST  | Yes  | Add a new fortune    |
| /api/v1/fortune/all | GET   | Yes  | Get all fortunes     |

For the calls that require authentication, a JWT bearer token is used. For
details on how to generate this JWT, see the section titled
[Generating JWT Signing Keys and Tokens](#generating-jwt-signing-keys-and-tokens).

For the POST to `/api/v1/fortune`, the JSON Schema of the request body can be
found in the `json-schema/fortune-api-post.schema.json` file. The following is
an example of that request body:

```
{
  "fortune": "Premature optimization is the root of all evil."
}
```

The response format is always the same. The schema can be found in the
`json-schema/fortune-api-response.schema.json` file. The following is an example
of a response:

```
{
  "status": "success",
  "code": 200,
  "message": "OK"
}
```

If a response has data, then a "data" property will be present with either an
object or an array of objects:

```
{
  "status": "success",
  "code": 200,
  "message": "OK"
  "data": {
     "fortune": "A smile is your passport into the hearts of others."
  }
}
```

## Build

This project is built using Java 17 and Maven 3.8.6. Java 17 was selected because
at the time of writing, it is the latest stable LTS release of Java. To build,
run the following:

```
mvn clean install
```

The build will kick off a series of static code checks using PMD and Checkstyle
and will result in a shadow jars in the `fortune-api/target` and
`fortune-api-client/target` directories.

## Configuration

The fortune-api application recognizes the following environment variables:

| Environment Variable       | Default                                  | Description                                                  |
|----------------------------|------------------------------------------|--------------------------------------------------------------|
| FORTUNE\_HTTP\_PORT        | 80                                       | The port to listen for HTTP connections                      |
| FORTUNE\_HTTPS\_PORT       | 443                                      | The port to listen for HTTPS connections                     |
| FORTUNE\_JDBC\_URL         | jdbc:sqlite:/fortune-data/fortune-api.db | The JDBC connect string                                      |
| FORTUNE\_JWT\_SIGNING\_KEY | `NULL`                                   | The JWT signing key                                          |
| FORTUNE\_LOG\_LEVEL        | INFO                                     | The logging level                                            |
| FORTUNE\_PUBLIC\_HOST      | `NULL`                                   | The publicly facing host name                                |
| FORTUNE\_SSL\_KEY          | `NULL`                                   | A Base64 encoded RSA private signing key for the certificate |
| FORTUNE\_SSL\_CERTS        | `NULL`                                   | A Base64 encoded PEM of the certificate chain                |

### Docker Build

Once the project is built, you can build the Docker container:

```
docker build -t fortune-api:local .
```

If you want to retain added fortunes, you will need a volume for the database:

```
docker volume create fortune-data
```

To run the container, use something like:

```
docker run -d --mount source=fortune-data,target=/fortune-data -p 80:80 --name fortune-api --rm fortune-api:local
```

To stop the container, use:

```
docker stop fortune-api
```

***Note:*** The `Dockerfile` is set up to build a container that runs natively on macOS running Apple Silicon. If you
are running on an Intel platform, you can replace the first two lines in the `Dockerfile` with:

```
FROM openjdk:17-alpine
```

### K8s Deployment

Sample deployment templates for use with `kubectl create` can be found in the `k8s` directory.

## Manual Testing

To run and test your own instance of the Fortune API, there are some tools that
can help. 

### Generating Self-Signed Certificates

In order to test using `https`, you will need a valid SSL certificate. Given
that most test environment do not have a certificate, you will most likely need
a self-signed certificate. If you look in the `FortuneApiClientTest` class you
will see one has already been generated, but if you need to make your own, here
are the steps: 

1. Generate keypair using the `openssl` command.

   ```
   openssl req \
   -x509 \
   -newkey rsa:2048 \
   -sha256 \
   -nodes \
   -keyout key.pem \
   -out cert.pem \
   -outform PEM \
   -days 3650 \
   -subj "/C=US/ST=Ohio/L=Urbana/O=Dan Shinton/OU=Engineering/CN=fortune.shinton.net"  \
   -addext "subjectAltName=DNS:localhost"
   ```

2. Prepare the private key. This is the value that should be configured to be returned by the
   `FortuneApiConfig.sslKey` call.

   ```
   cat key.pem | base64
   ```

3. Prepare the certificate chain. This is the value that should be returned by the `sslCerts` calls in the
   `FortuneApiConfig` and `FortuneApiClientConfig` classes.

   ```
   cat cert.pem | base64
   ```

### Generating JWT Signing Keys and Tokens

For commands that require authentication, a JWT bearer token must be sent in the `Authorization` header of the request.
This means that the Fortune API has to be configured with a signing key to validate bearer tokens created with that
signing key. To generate a signing key, you can use the `BearerTokenTool` CLI.

```
$ java -cp fortune-api.jar net.shinton.util.BearerTokenTool key
7WXemkbNxnYue2TjjAHiWdWu60mgFAw9gTZ8zXylPD2hvZS1Mw7gK8MLghyLrhoVx4b4zLEcbNrA7W+7uuqgyg==
```

This is the value that should be configured to be returned by the `FortuneApiConfig.jwtSigningKey` call. That is also
the value that is passed into the token generator.

```
$ java -cp fortune-api.jar net.shinton.util.BearerTokenTool token -p "localhost" -k "7WXemkbNxnYue2TjjAHiWdWu60mgFAw9gTZ8zXylPD2hvZS1Mw7gK8MLghyLrhoVx4b4zLEcbNrA7W+7uuqgyg=="
Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzaGludG9uLm5ldCIsImlhdCI6MTY2NzI0MDIwOCwiYXVkIjoibG9jYWxob3N0L2FwaS92MS9mb3J0dW5lIiwiZXhwIjoxNjY3MzI2NjA4fQ.fMo-rcjbDdJmFahpVTpsno2jFeWrrTahsogpfRxkyjN4iiPQHMOFlRdnE740cP5b1q9YA7o6zzoZPpCqkxpe4Q
```

#### Replay attack

Based on the way our bearer tokens are structured, are they susceptible to replay attacks?

Yes. Most definitely. Therefore, care must be taken in their regard. If this was
more than an example project, a more secure method of authentication would be
utilized. Having said that, you can significantly mitigate the risk by doing the
following:

1. Only make calls over the internet via HTTPS.
2. Do not check the signing key into the repo unencrypted.
3. Do not generate JWTs with long expirations. Expirations should only be long
   enough to do your immediate work and then expire.
4. If a JWT gets out in the wild, immediately change the signing key to
   invalidate it.

### Fortune API Client

The Fortune API client library can be used by a Java program to query the
Fortune API or it can be invoked directly on the command line. For example, to
get a new fortune, use:

```
$ java -jar fortune-api-client.jar get -u http://localhost
Land is always on the mind of a flying bird.
```

Some require a bearer token. For example, adding a fortune:

```
$ java -jar fortune-api-client.jar add -u http://localhost -f "Premature optimization is the root of all evil" -a "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc..."
Fortune successfully added
```

For more information about available commands and options, just ask for help:

```
$ java -jar fortune-api-client.jar --help
```

If you don't want to use the client, you can query the API using cURL or
the IntelliJ HttpClient service. Here are the previous two examples in cURL:

```
$ curl -s http://localhost/api/v1/fortune | jq -r '.data.fortune'
Wealth awaits you very soon.

$ curl -X POST \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc...' \
  -d '{"fortune":"Premature optimization is the root of all evil"}' \
  http://localhost/api/v1/fortune
{"status":"success","code":201,"message":"Created"}
```

### HTTP Client

IntelliJ has a feature called [HTTP Client](https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html).
It allows you to specify a file containing HTTP calls that make testing simple.
In the `http-client` directory of the project, you will find the HTTP Client
files for the fortune-api to aid in testing.
