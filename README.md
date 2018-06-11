# Summary

Project uses Scala, Play & Akka. My Scala skills are quite rust and I never used Play before,
so it forced me to do some research while coding, and as in this case patterns are not engraved in my brain,
I guess there is a lot of non idiomatic solutions.

## Execute

```
sbt compile
sbt test
sbt run
```

## Routes

```
POST    /api/offer                  controllers.OfferController.create
GET     /api/offer                  controllers.OfferController.list(all: Option[Boolean])
GET     /api/offer/:id              controllers.OfferController.read(id: UUID)
PATCH   /api/offer/:id/cancel       controllers.OfferController.cancel(id: UUID)
```

there is `test/fiddle.rest` file which can be used to quickly explorer API surface using Visual Studio Code and
[REST Client](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) plugin.

## Design considerations

I had a lot of question while implementing this solution. There was a lot of accidental complexity
(JSON serialization, dependency injection, unit testing) partially cause by my limited exposure to this tech stack,
but also some incidental complexity related to actual problem.

* Should manual cancellation be a PATCH or POST? We may POST a cancellation or PATCH offer by modifying cancellation data.
* Cancellation cancels offer *now* but could actually take a date in query string
* Money type could use limited set of allowed currencies, but currency code has been eventually implemented as free-form text
* InMemoryOfferRepository is not thread-safe, thread safety of while solution comes from the fact that there a single actor handling all requests. Better solution should use Router with consistent hashing but I assumed it is not in scope
* There are some mini-patterns which has been discovered while implementing (ie: OfferController.fromBody, OfferController.fromUrl, OfferActor.respond). I guess they are somewhere in the framework so most likley I was reinventing the wheel. I needed them to reduce clutter in "business" code.
* I commited to use "joda.time" but as far as I understand now the new go-to solution is "java.time" from Java 8.

## Virtual time

I like the idea of virtual time. I was using it with Reactive Extensions and here it was implemented with very simplistic trait `TimeProvider`. All operation use it to determine what time it is now, so by injecting fake `TimeProvider` (`TestTimeProvider`) we can simulate "how it will look 2 day from now".

## Running on Unix

I was implementing it on Windows machine therefore it is possible that some file attributes could be lost, for example, to fix `sbt` you may need to run `chmod +x sbt`.
