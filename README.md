jeve
====

jeve is a lightweight Java 8 event dispatching framework which takes advantage
of lambda expressions and internal iteration. It makes it really simple to
implement the observer pattern without much overhead in code while granting some
great additional features. jeve explained in one Java statement:
```java
eventProvider.dispatch(UserListener.class, new UserEvent(this, user), UserListener::userAdded);
```


## License
jeve is distributed under the MIT License. See `LICENSE.md` in this directory
for detailed information.



## Documentation
JavaDoc is available at www.jeve.skuzzle.de/1.1.0/doc

Scroll down in this readme for a quick start guide and some advanced topics.

Further support can be found in IRC (irc.euirc.net, #pollyisawesome) or via
Twitter (@ProjectPolly).



## Building
Building jeve requires Apache Maven to be installed. You may then run 
`mvn install` to build jeve and install it into your local repository. If you 
want to add jeve to your existing projects, declare the following dependency 
within your `pom.xml`:

```xml
    <dependency>
      <groupId>de.skuzzle</groupId>
      <artifactId>jeve</artifactId>
      <version>1.1.0</version>
    </dependency>
```



# Why jeve?
jeve avoids client code from ending up cluttered with event delegation routines 
like in the following **bad practice** example.

```java
public class UserManager {
    
    private List<UserListener> listeners = new ArrayList<>();
    
    // ...
    
    public void addUser(User user) {
        // logic for adding a user goes here
        // ...
        // now notify our listeners
        for (UserListener listener : this.listeners) {
            listener.userAdded(user);
        }
    }
    
    public void deleteUser(User user) {
        // logic for deleting a user goes here
        // ...
        // now notify our listeners
        for (UserListener listener : this.listeners) {
            listener.userDeleted(user);
        }
    }
}
```

This sample code has several weaknesses:
* duplicated for-loop statements for each event type
* no handling of errors during event delegation
* not parallelizable
* process of event delegation can not be aborted
* If the UserManager class should fire another type of events to another type
  of listeners, we would need to create a second list which holds the other 
  listener kinds.

Most of these weaknesses can be solved by using *internal iteration*. That is,
moving iteration **into** the framework, making it transparent for the caller. 
See the quick start guid below to learn how jeve addresses these weaknesses.


# Quickstart
Using jeve for simple event dispatching is rather simple. It involves creating
an `EventProvider` as first step:

```java
import de.skuzzle.jeve.EventProvider;

public class UserManager {
    // The default event provider dispatches events sequentially within
    // the current thread.
    private final EventProvider events = EventProvider.newDefaultEventProvider();
}
```

Next, you should create an event class and a listener interface:

```java
import de.skuzzle.jeve.Event;

public class UserEvent extends Event<UserManager> {
    private final User user;
    
    public UserEvent(UserManager source, User user) {
        super(source);
        this.user = user;
    }
    
    public User getUser() {
        return this.user;
    }
}
```

```java
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;

@ListenerInterface
public interface UserListener extends Listener {
    public void userAdded(UserEvent e);
    public void userDeleted(UserEvent e);
}
```

Now, you may hook the parts together. Add methods to the `UserManager` class
to add and remove listeners. Then add methods that actually fire events to
notify registered listeners.

```java
// ...

public class UserManager {
    private final EventProvider events = EventProvider.newDefaultEventProvider();
    
    public void addUserListener(UserListener listener) {
        this.events.addListener(UserListener.class, listener);
    }
    
    public void removeUserListener(UserListener listener) {
        this.events.removeListener(UserListener.class, listener);
    }
    
    public void addUser(User user) {
        // logic for adding the user goes here
        // ...
        // now notify the listeners
        final UserEvent e = new UserEvent(this, user);
        this.events.dispatch(UserListener.class, e, UserListener::userAdded);
    }
    
    public void deleteUser(User user) {
        // same here
        // ...
        this.events.dispatch(UserListener.class, e, UserListener::userDeleted);
    }
}
```

With jeve, all the above listed flaws can be treated in a safe and clear way:

* only one statement needed for firing a single event
* errors can be reported on a different channel, not interrupting the event 
  delegation (see below)
* by simply obtaining a different EventProvider implementation, event 
  dispatching can be parallelized without touching any existing code
* the event delegation process can be stopped by setting the event to be handled
  (see below)
* the EventProvider internally manages different kinds of listeners.



# Advanced Topics

## Stop event delegation
Listeners are notified in order they have been registered with the 
`EventProvider`. If you want to stop the delegation of an event to further 
listeners, you may use the `Event.setHandled(boolean)` method.

```java
// ...
@ListenerInterface
public class SampleUserListener implements UserListener {
    @Override
    public void userAdded(UserEvent e) {
        // do something
        // ...
        // now, stop delegation process by setting the event to be "handled".
        // No further listeners will be notified about this event.
        e.setHandled(true);
    }
    
    @Override
    public void userDeleted(UserEvent e) {
        // ...
    }
}
```

Additionally, when using a single threaded EventProvider, your listening method 
could throw an `AbortionException` to brutally stop delegation.


## Automatically remove listeners
Automatically removing listeners from an EventProvider as of jeve version 1.0.0
is now deprecated, because it was inconvenient to use and error prone. Instead,
you should remove the listener manually from its source.

```java
// ...
public class SampleUserListener implements UserListener {
 
    @Override
    public void userAdded(UserEvent e) {
        // do something
        // ...
        // this listener should not be notified about further events anymore
        // Note: Event's source must support removing the listener
        e.getSource().removeUserListener(this);
    }
    
    @Override
    public void userDeleted(UserEvent e) {
        // ...
    }
}
```


## Errors during event delegation
All provided `EventProvider` implementations provide error tolerant event 
delegation. That is, if any notified listener throws a `RuntimeException`, this
exception will be ignored and the next listener is notified. However, instead
if ignoring the exception, you might want to customize the reaction. There 
exists an overload of the `EventProvider.dispatch` method which takes an 
`ExceptionCallback` as additional argument. This class has a single method to 
which exceptions get passed.

```java
    // ...
    public void addUser(User user) {
        // logic for adding the user goes here
        // ...
        // now notify the listeners. We pass an ExceptionCallback as lambda 
        // expression which uses a logger to log any exception that occurred.
        final UserEvent e = new UserEvent(this, user);
        this.events.dispatch(UserListener.class, e, UserListener::userAdded,
            (e, l, ev) -> logger.error("Exception occurred during event dispatching", e));
    }
}
```

You may also set an ExceptionCallback globally for a specific EventProvider 
instance using `EventProvider.setExceptionCallback()`. When doing so, the 
provided callback will be notified when dispatching an event without explicitly 
specifying a callback.

EventProviders will swallow any exception thrown by the ExceptionCallback 
except AbortionExceptions. Those will be passed to the caller.

## Asynchronous event delegation
One key feature of jeve is that it hides the event delegation strategy 
from the actual source of the event. So if you decide that all of your 
`UserEvents` should be fired within a dedicated event thread, you simply need 
to modify the creation of the `EventProvider`:

```java
// ...
import java.util.concurrent.ExecutorService;

public class UserManager {
    // Executor which will be used to fire events
    private final ExecutorService eventService = Executors.newSingleThreadExecutor();
    
    // EventProvider which will use the executor to fire events asynchronously
    private final EventProvider events = 
        EventProvider.newAsynchronousEventProvider(eventService);
        
    // remaining code stays the same
    // ...
}
```

jeve also supports the creation of GUI events by providing EventProvider 
implementations which run all listeners within the AWT Event Thread.

## Implementing own dispatching strategies
If you want to customize the process of event dispatching, you can create your
own `EventProvider` by extending `AbstractEventProvider` and overriding the
`dispatch(Class, Event, BiConsumer, ExceptionCallback)` method. Within that 
method you can use `EventProvider.getListeners` to get a collection of all 
registered listeners for a specified class.

# Tests
jeve comes with a set of blackboard test for the EventProvider interface. If 
you create your own EventProvider class and want it to be tested against the 
default interface specification, you can extend the existing class 
`EventProviderTestBase`. Please see the existing test cases in 
`src/test/java/de/skuzzle/jeve`
