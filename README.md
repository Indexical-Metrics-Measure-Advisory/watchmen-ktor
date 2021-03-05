# watchmen-ktor

watchmen-ktor is your new project powered by [Ktor](http://ktor.io) framework.

<img src="https://repository-images.githubusercontent.com/40136600/f3f5fd00-c59e-11e9-8284-cb297d193133" alt="Ktor" width="100" style="max-width:20%;">

Company website: imma.com Ktor Version: 1.5.2 Kotlin Version: 1.4.10
BuildSystem: [Gradle with Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)

# Ktor Documentation

Ktor is a framework for quickly creating web applications in Kotlin with minimal effort.

* Ktor project's [Github](https://github.com/ktorio/ktor/blob/master/README.md)
* Getting started with [Gradle](http://ktor.io/quickstart/gradle.html)
* Getting started with [Maven](http://ktor.io/quickstart/maven.html)
* Getting started with [IDEA](http://ktor.io/quickstart/intellij-idea.html)

Selected Features:

* [Routing](#routing-documentation-jetbrainshttpswwwjetbrainscom)
* [CallLogging](#calllogging-documentation-jetbrainshttpswwwjetbrainscom)
* [Shutdown URL](#shutdown-url-documentation-jetbrainshttpswwwjetbrainscom)
* [ContentNegotiation](#contentnegotiation-documentation-jetbrainshttpswwwjetbrainscom)
* [Jackson](#jackson-documentation-jetbrainshttpswwwjetbrainscom)
* [Metrics](#metrics-documentation-jetbrainshttpswwwjetbrainscom)

## Routing Documentation ([JetBrains](https://www.jetbrains.com))

Allows to define structured routes and associated handlers.

### Description

Routing is a feature that is installed into an Application to simplify and structure page request handling. This page
explains the routing feature. Extracting information about a request, and generating valid responses inside a route, is
described on the requests and responses pages.

```application.install(Routing) {
    get("/") {
        call.respondText("Hello, World!")
    }
    get("/bye") {
        call.respondText("Good bye, World!")
    }

```

`get`, `post`, `put`, `delete`, `head` and `options` functions are convenience shortcuts to a flexible and powerful
routing system. In particular, get is an alias to `route(HttpMethod.Get, path) { handle(body) }`, where body is a lambda
passed to the get function.

### Usage

## Routing Tree

Routing is organized in a tree with a recursive matching system that is capable of handling quite complex rules for
request processing. The Tree is built with nodes and selectors. The Node contains handlers and interceptors, and the
selector is attached to an arc which connects another node. If selector matches current routing evaluation context, the
algorithm goes down to the node associated with that selector.

Routing is built using a DSL in a nested manner:

```kotlin
route("a") { // matches first segment with the value "a"
  route("b") { // matches second segment with the value "b"
     get {…} // matches GET verb, and installs a handler
     post {…} // matches POST verb, and installs a handler
  }
}
```

```kotlin
method(HttpMethod.Get) { // matches GET verb
   route("a") { // matches first segment with the value "a"
      route("b") { // matches second segment with the value "b"
         handle { … } // installs handler
      }
   }
}
```kotlin
route resolution algorithms go through nodes recursively discarding subtrees where selector didn't match.

Builder functions:
* `route(path)` – adds path segments matcher(s), see below about paths
* `method(verb)` – adds HTTP method matcher.
* `param(name, value)` – adds matcher for a specific value of the query parameter
* `param(name)` – adds matcher that checks for the existence of a query parameter and captures its value
* `optionalParam(name)` – adds matcher that captures the value of a query parameter if it exists
* `header(name, value)` – adds matcher that for a specific value of HTTP header, see below about quality

## Path
Building routing tree by hand would be very inconvenient. Thus there is `route` function that covers most of the use cases in a simple way, using path.

`route` function (and respective HTTP verb aliases) receives a `path` as a parameter which is processed to build routing tree. First, it is split into path segments by the `/` delimiter. Each segment generates a nested routing node.

These two variants are equivalent:

```kotlin
route("/foo/bar") { … } // (1)

route("/foo") {
   route("bar") { … } // (2)
}
```

### Parameters

Path can also contain parameters that match specific path segment and capture its value into `parameters` properties of
an application call:

```kotlin
get("/user/{login}") {
   val login = call.parameters["login"]
}
```

When user agent requests `/user/john` using `GET` method, this route is matched and `parameters` property will
have `"login"` key with value `"john"`.

### Optional, Wildcard, Tailcard

Parameters and path segments can be optional or capture entire remainder of URI.

* `{param?}` –- optional path segment, if it exists it's captured in the parameter
* `*` –- wildcard, any segment will match, but shouldn't be missing
* `{...}` –- tailcard, matches all the rest of the URI, should be last. Can be empty.
* `{param...}` –- captured tailcard, matches all the rest of the URI and puts multiple values for each path segment
  into `parameters` using `param` as key. Use `call.parameters.getAll("param")` to get all values.

Examples:

```kotlin
get("/user/{login}/{fullname?}") { … }
get("/resources/{path...}") { … }
```

## Quality

It is not unlikely that several routes can match to the same HTTP request.

One example is matching on the `Accept` HTTP header which can have multiple values with specified priority (quality).

```kotlin
accept(ContentType.Text.Plain) { … }
accept(ContentType.Text.Html) { … }
```

The routing matching algorithm not only checks if a particular HTTP request matches a specific path in a routing tree,
but it also calculates the quality of the match and selects the routing node with the best quality. Given the routes
above, which match on the Accept header, and given the request header `Accept: text/plain; q=0.5, text/html` will
match `text/html` because the quality factor in the HTTP header indicates a lower quality fortext/plain (default is 1.0)
.

The Header `Accept: text/plain, text/*` will match `text/plain`. Wildcard matches are considered less specific than
direct matches. Therefore the routing matching algorithm will consider them to have a lower quality.

Another example is making short URLs to named entities, e.g. users, and still being able to prefer specific pages
like `"settings"`. An example would be

* `https://twitter.com/kotlin` -– displays user `"kotlin"`
* `https://twitter.com/settings` -- displays settings page

This can be implemented like this:

```kotlin
get("/{user}") { … }
get("/settings") { … }
```

The parameter is considered to have a lower quality than a constant string, so that even if `/settings` matches both,
the second route will be selected.

### Options

No options()

## CallLogging Documentation ([JetBrains](https://www.jetbrains.com))

Logs client requests

### Description

You might want to log client requests: and the Call Logging feature does just that. It uses
the `ApplicationEnvironment.log(LoggerFactory.getLogger("Application"))` that uses `slf4j` so you can easily configure
the output. For more information on logging in Ktor, please check the logging in the ktor page.

### Usage

## Basic usage

The basic unconfigured feature logs every request using the level `TRACE`:

```kotlin
install(CallLogging)

```

## Configuring

This feature allows you to configure the log level and filtering the requests that are being logged:

```kotlin
install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/section1") }
    filter { call -> call.request.path().startsWith("/section2") }
    // ...
}

```

The filter method keeps an allow list of filters. If no filters are defined, everything is logged. And if there are
filters, if any of them returns true, the call will be logged.

In the example, it will log both: `/section1/*` and `/section2/*` requests.

## MDC

The `CallLogging` feature supports `MDC` (Mapped Diagnostic Context) from `slf4j` to associate information as part of
the request.

When installing the `CallLogging`, you can configure a parameter to associate to the request with the mdc method. This
method requires a key name, and a function provider. The context would be associated (and the providers will be called)
as part of the `Monitoring` pipeline phase.

```kotlin
install(CallLogging) {
    mdc(name) { // call: ApplicationCall ->
        "value"
    }
    // ...
}

```

### Options

No options()

## Shutdown URL Documentation ([JetBrains](https://www.jetbrains.com))

This feature enables a URL that when accessed, shutdowns the server.

### Description

This feature enables a URL that when accessed, shutdowns the server.

There are two ways to use it: Automatically using `HOCON` and Installing the feature

### Usage

You can manually install the feature, with `ShutDownUrl.ApplicationCallFeature` and set the `shutDownUrl` and
an `exitCodeSupplier`:

```kotlin
install(ShutDownUrl.ApplicationCallFeature) {
    // The URL that will be intercepted
    shutDownUrl = "/ktor/application/shutdown"
    // A function that will be executed to get the exit code of the process
    exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
}
```

You can configure a shutdown URL using `HOCON` with the `ktor.deployment.shutdown.url` property.

```kotlin
ktor {
    deployment {
        shutdown.url = "/my/shutdown/path"
    }
}
```

### Options

* `shutDownUrl` -- The URL that will be intercepted
* `exitCodeSupplier` -- A function that will be executed to get the exit code of the process()

## ContentNegotiation Documentation ([JetBrains](https://www.jetbrains.com))

Provides automatic content conversion according to Content-Type and Accept headers.

### Description

The `ContentNegotiation` feature serves two primary purposes:

* Negotiating media types between the client and server. For this, it uses the `Accept` and `Content-Type` headers.
* Serializing/deserializing the content in the specific format, which is provided by either the
  built-in `kotlinx.serialization` library or external ones, such as `Gson` and `Jackson`, amongst others.

### Usage

## Installation

To install the `ContentNegotiation` feature, pass it to the `install` function in the application initialization code.
This can be the `main` function ...

```kotlin
import io.ktor.features.*
// ...
fun Application.main() {
  install(ContentNegotiation)
  // ...
}
```

... or a specified `module`:

```kotlin
import io.ktor.features.*
// ...
fun Application.module() {
    install(ContentNegotiation)
    // ...
}
```

## Register a Converter

To register a converter for a specified `Content-Type`, you need to call the register method. In the example below, two
custom converters are registered to deserialize `application/json` and `application/xml` data:

```kotlin
install(ContentNegotiation) {
    register(ContentType.Application.Json, CustomJsonConverter())
    register(ContentType.Application.Xml, CustomXmlConverter())
}
```

### Built-in Converters

Ktor provides the set of built-in converters for handing various content types without writing your own logic:

* `Gson` for JSON

* `Jackson` for JSON

* `kotlinx.serialization` for JSON, Protobuf, CBOR, and so on

See a corresponding topic to learn how to install the required dependencies, register, and configure a converter.

## Receive and Send Data

### Create a Data Class

To deserialize received data into an object, you need to create a data class, for example:

```kotlin
data class Customer(val id: Int, val firstName: String, val lastName: String)
```

If you use `kotlinx.serialization`, make sure that this class has the `@Serializable` annotation:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class Customer(val id: Int, val firstName: String, val lastName: String)
```

### Receive Data

To receive and convert a content for a request, call the `receive` method that accepts a data class as a parameter:

```kotlin
post("/customer") {
    val customer = call.receive<Customer>()
}
```

The `Content-Type` of the request will be used to choose a converter for processing the request. The example below shows
a sample HTTP client request containing JSON data that will be converted to a `Customer` object on the server side:

```kotlin
post http://0.0.0.0:8080/customer
Content-Type: application/json

{
  "id": 1,
  "firstName" : "Jet",
  "lastName": "Brains"
}
```

### Send Data

To pass a data object in a response, you can use the `respond` method:

```kotlin
post("/customer") {
    call.respond(Customer(1, "Jet", "Brains"))
}
```

In this case, Ktor uses the `Accept` header to choose the required converter.

## Implement a Custom Converter

In Ktor, you can write your own converter for serializing/deserializing data. To do this, you need to implement
the `ContentConverter` interface:

```kotlin
interface ContentConverter {
    suspend fun convertForSend(context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any): Any?
    suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any?
}
```

Take a look at
the [GsonConverter](https://github.com/ktorio/ktor/blob/master/ktor-features/ktor-gson/jvm/src/io/ktor/gson/GsonSupport.kt)
class as an implementation example.

### Options

No options()

## Jackson Documentation ([JetBrains](https://www.jetbrains.com))

Handles JSON serialization using Jackson library

### Description

ContentNegotiation provides the built-in `Jackson` converter for handing JSON data in your application.

### Usage

To register the `Jackson` converter in your application, call the `jackson` method:

```kotlin
import io.ktor.jackson.*

install(ContentNegotiation) {
    jackson()
}
```

Inside the `jackson` block, you can access
the [ObjectMapper](https://fasterxml.github.io/jackson-databind/javadoc/2.9/com/fasterxml/jackson/databind/ObjectMapper.html)
API, for example:

```kotlin
install(ContentNegotiation) {
    jackson {
        enable(SerializationFeature.INDENT_OUTPUT)
        dateFormat = DateFormat.getDateInstance()
        // ...
    }
}
```

### Options

No options()

## Metrics Documentation ([JetBrains](https://www.jetbrains.com))

Adds supports for monitoring several metrics

### Description

The Metrics feature allows you to configure the Metrics to get useful information about the server and incoming
requests.

### Usage

The Metrics feature exposes a `registry` property, that can be used to build and start metric reporters.

## JMX Reporter

The JMX Reporter allows you to expose all the metrics to JMX, allowing you to view those metrics with `jconsole`
or `jvisualvm` with the MBeans plugin.

```kotlin
install(DropwizardMetrics) {
    JmxReporter.forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build()
        .start()
}

``` 

## SLF4J Reporter

The SLF4J Reporter allows you to periodically emit reports to any output supported by SLF4J. For example, to output the
metrics every 10 seconds, you would:

```kotlin
install(DropwizardMetrics) {
    Slf4jReporter.forRegistry(registry)
        .outputTo(log)  
        .convertRatesTo(TimeUnit.SECONDS)  
        .convertDurationsTo(TimeUnit.MILLISECONDS)  
        .build()  
        .start(10, TimeUnit.SECONDS)
}

```

## Other reporters

You can use any of the available [Metric reporters](http://metrics.dropwizard.io/4.0.0/).

## Exposed reports

This feature exposes many JVM properties relating to memory usage and thread behavior.

#### Global:

Specifically to Ktor, it exposes:

* `ktor.calls.active`:`Count` - The number of unfinished active requests
* `ktor.calls.duration` - Information about the duration of the calls
* `ktor.calls.exceptions` - Information about the number of exceptions
* `ktor.calls.status.NNN` - Information about the number of times that happened a specific HTTP Status Code NNN

#### Per endpoint:

* `"/uri(method:VERB).NNN"` - Information about the number of times that happened a specific HTTP Status Code NNN, for
  this path, for this verb
* `"/uri(method:VERB).meter"` - Information about the number of calls for this path, for this verb
* `"/uri(method:VERB).timer"` - Information about the durations for this endpoint

## Information per report

#### Durations

`"/uri(method:VERB).timer"` and `ktor.calls.duration` are durations and expose:

* 50thPercentile
* 75thPercentile
* 95thPercentile
* 98thPercentile
* 99thPercentile
* 999thPercentile
* Count
* DurationUnit
* OneMinuteRate
* FifteenMinuteRate
* FiveMinuteRate
* Max
* Mean
* MeanRate
* Min
* RateUnit
* StdDev

#### Counts

The other properties are exposed as counts:

* Count
* FifteenMinuteRate
* FiveMinuteRate
* OneMinuteRate
* MeanRate
* RateUnit

### Options

No options()

# Reporting Issues / Support

Please use [our issue tracker](https://youtrack.jetbrains.com/issues/KTOR) for filing feature requests and bugs. If
you'd like to ask a question, we recommmend [StackOverflow](https://stackoverflow.com/questions/tagged/ktor) where
members of the team monitor frequently.

There is also community support on the [Kotlin Slack Ktor channel](https://app.slack.com/client/T09229ZC6/C0A974TJ9)

# Reporting Security Vulnerabilities

If you find a security vulnerability in Ktor, we kindly request that you reach out to the JetBrains security team via
our [responsible disclosure process](https://www.jetbrains.com/legal/terms/responsible-disclosure.html).

# Contributing

Please see [the contribution guide](CONTRIBUTING.md) and the [Code of conduct](CODE_OF_CONDUCT.md) before contributing.

TODO: contribution of features guide (link)