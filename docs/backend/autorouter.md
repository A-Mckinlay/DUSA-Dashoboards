# Autorouter
_Witty name not included._

[Autorouter](https://github.com/Emberwalker/spark-autorouter) handles route generation in Dashoboards. It supplements
Sparks route handling with annotation-driven routing, to avoid a central file that gets packed with junk routes.

## Routing for Dummies - Routes
First make your method roughly look like `public static Object methodNameHere(Request, Response)` (changing the name,
and naming the Request/Response params) - Note that the Request and Response classes are the ones in the `spark`
package, not any other one!

Once that's done, apply the appropriate annotation from `io.drakon.spark.autorouter.Routes` e.g. `Routes.GET`. The route
annotations all require a `path = "/whatever"` value, and can optionally take an Accept Type and `ResponseTransformer`
class.

## Filters
Filters are similar, but more flexible. Before reading on, check the Spark docs for Filters. Done? Good. The method
signature form is identical to that of a route, but with a different annotation.

Before and After filters (`@Routes.Before` and `@Routes.After` respectively) _optionally_ take a `path` and an Accept
Type - Filters without a path will be applied to all requests. Be _careful_ writing global filters - you may have to
think about security implications of interfering with the Syn filters.

After After filters (`@Routes.AfterAfter`) can only (optionally) take a path, and run after Before _and_ After filters.

## Exception Handlers
Something's gone wrong in a route. You want Spark to handle it in a way that isn't just a `500 Internal Server Error`.
You need a `@Routes.ExceptionHandler`!

Exception handlers take one parameter, `exceptionType`, which is the `Class` object of the exception you want to catch.
Note that using this will also catch subclasses of this type, so be very careful writing one that catches
`Exception.class`!

The method signature for exception handlers is
`public static Object methodNameHere(ExceptionTypeHere, Request, Response)` - Replace `ExceptionTypeHere` with whatever
exception class you passed to the annotation.