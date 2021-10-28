# Twitter Notification Streams

### Tools used

* **Java 14**: Access to Streams, modern Java functional methods
* **Lombok**: Used to ease the amount of Boilerplate needed
* **Spring Boot**: Usually a framework that allows for DI minimises the amount of Boilerplate code needed.   
* **Reactor**: Reactor was chosen in order to enable easy multicasting, functional methods to convert things, and easy batching. 

### Design 

As described above, it uses a lot of reactive programming paradigms. 

The reason this was chosen was because the requirements called for extensibility, threading, etc.  

We are able to pass around the Flux<TweetData> easily. This in turn allowed me to create multiple consumers, one for the stats, for instance, but the other for the printing. 

The threading aspect of this was also made simpler. We can have one background thread producing the methods, and easily multicast this to other observers. 



