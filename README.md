[![Build Status](https://travis-ci.org/skuzzle/jeve.svg?branch=develop)](https://travis-ci.org/skuzzle/jeve)
[SonarQube](https://www.serverd.de/sonar/dashboard/index/364)

jeve
====

jeve (pronounced "_jieve_") is a lightweight Java 8 event dispatching framework which 
takes advantage of lambda expressions and internal iteration. It makes it really simple 
to implement the observer pattern without much overhead in code while granting some
great additional features. jeve explained in one Java statement:
```java
eventProvider.dispatch(new UserEvent(this, user), UserListener::userAdded);
```

## License
jeve is distributed under the MIT License. See `LICENSE.md` in this directory
for detailed information.

## Documentation
JavaDoc is available at www.jeve.skuzzle.de/2.0.x/doc

Scroll down in this readme for a quick start guide and some advanced topics.

Further support can be found in IRC (irc.euirc.net, #pollyisawesome) or via
Twitter (@ProjectPolly).

## Maven Dependency
Jeve is available as dependency for your projects through Maven's Central Repository:

```xml
    <dependency>
        <groupId>de.skuzzle</groupId>
        <artifactId>jeve</artifactId>
        <version>2.0.2</version>
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
See the quick start guid below to learn how jeve addresses these issues.

# Design Principles
jeve is built around some design principles and abstractions that should be heard of 
before starting to use jeve.

* jeve decouples the knowledge of where listener come from from the knowledge of how to 
  notify a listener
* jeve also decouples the knowledge of how to handle an error from the knowledge of how to
  notify a listener
  
These are the main abstractions that jeve use:

| Class    | Description |
| -------- | ----------- |
| Listener       | An object that is notified about a certain `Event`          |
| Event          | Object that is passed to a `Listener` by being 'dispatched' |
| ListenerSource | Supplies listeners to be notified to an `EventProvider`     |
| ListenerStore  | Modifiable extension to `ListenerSource` to which listeners can be added and removed |
| EventProvider  | Notifies listeners supplied by a `ListenerSource` about an `Event`

# Quickstart
Using jeve for simple event dispatching is rather simple. It involves creating
an `EventProvider` as first step:

```java
import de.skuzzle.jeve.EventProvider;

public class UserManager {
    // Dispatches events sequentially within the current thread
    private final EventProvider events = EventProvider.createDefault();
}
```

Next, you should create an event class and a listener interface:

```java
import de.skuzzle.jeve.Event;

public class UserEvent extends Event<UserManager, UserListener> {
    private final User user;

    public UserEvent(UserManager source, User user) {
        super(source, UserListener.class);
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }
}
```

Notice that each Event implementation is aware of the type and the class of the
Listener which is able to handle it. This also implies that you can not
implement two different listeners which handle the same kind of event.

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
    private final EventProvider<?> events = EventProvider.createDefault();

    public void addUserListener(UserListener listener) {
        this.events.listeners().add(UserListener.class, listener);
    }

    public void removeUserListener(UserListener listener) {
        this.events.listeners().remove(UserListener.class, listener);
    }

    public void addUser(User user) {
        // logic for adding the user goes here
        // ...
        // now notify the listeners
        final UserEvent e = new UserEvent(this, user);
        this.events.dispatch(e, UserListener::userAdded);
    }

    public void deleteUser(User user) {
        // same here
        // ...
        this.events.dispatch(e, UserListener::userDeleted);
    }
}
```

With jeve, all the above listed flaws can be treated in a safe and clear way:

* only one statement needed for firing a single event
* errors can be reported on a different channel, not interrupting the event
  delegation (see below)
* by simply obtaining a different EventProvider implementation, event
  dispatching can be parallelized without touching any existing code
* the event delegation process can be stopped gracefully by setting the event 
  to be _handled_ (see below)
* the EventProvider internally manages different kinds of listeners.

# Advanced Topics

## Listener Stores
In jeve 2.0.0, listeners are managed by an implementation of `ListenerStore`, 
an instance of which is passed to an EventProvider at construction time. When
dispatching an event, the EventProvider queries the store to obtain a `Stream`
of listeners which should be notified. This grants the ability to share a 
single store between multiple providers and to re-order listeners before 
delivering them to the provider. For example, jeve comes with a 
`PriorityListenerStore`, which allows to order listeners by a priority value 
before passing them to the EventProvider:

```java
    EventProvider<PriorityListenerStore> events = EventProvider.configure()
        .store(new PriorityListenerStore())
        .useSynchronousProvider()
        .create();

    ...
    events.listeners().add(UserListener.class, myListener, 2);
    events.listeners().add(UserListener.class, myOtherListener, 1);
```
In this example, `myOtherListener` would always be notified before `myListener` 
because it has a lower priority value.

## Default Target Events
A common case in real life applications is, that listener interfaces only 
contain a single listening method. If this is the case, always specifying the method 
reference when dispatching an event is verbose and inconvenient. Instead, you 
may use `DefaultDispatchEvent` which is fully compatible with normal events. It
allows to statically provide a default dispatch method and can be used with an overload 
of `EventProvider.dispatch`:

```java
public class UserEvent extends DefaultDispatchEvent<UserManager, UserListener> {

    public UserEvent(UserManager source) {
        super(source, UserListener.class);
    }
    
    @Override
    public void defaultDispatch(EventProvider<?> provider, ExceptionCallback ec) {
        // Use double-dispatch approach to dispatch this event with the given provider
        provider.dispatch(this, UserListener::userAdded, ec);
    }
}
```
Dispatching this event is as easy as:

```java
    UserEvent e = new UserEvent(userManager);
    eventProvider.dispatch(e);
```


## Stopping event delegation
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
could throw an `AbortionException` to brutally stop delegation. This method is
to be handled with care, as it is the only way to delegate an exception up to
the caller of `dispatch` and may thus interrupt the client's control flow. This
subverts jeve's goal of never interrupting the event delegation process and
should only be used in exceptional cases.

The behavior of both methods for aborting the delegation process is generally
undefined for multi-threaded and non-sequential EventProvider implementations.

## Automatically remove listeners
jeve 2.0.0 provides a convenient way to remove a listener which is currently 
being notified from the listener store it was retrieved from. Thus, this 
listener won't be notified again:

```java
// ...
public class SampleUserListener implements UserListener {

    @Override
    public void userAdded(UserEvent e) {
        // do something
        // ...
        // this listener should not be notified about further events anymore
        e.stopNotifying(this);
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
of ignoring the exception, you might want to customize the reaction. There 
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
        this.events.dispatch(e, UserListener::userAdded,
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
from the actual source of the event. If you decide that all of your
`UserEvents` should be fired within a dedicated event thread, you simply need
to modify the creation of the `EventProvider`:

```java
// ...
import java.util.concurrent.ExecutorService;

public class UserManager {
    // Executor which will be used to fire events
    private final ExecutorService eventService = Executors.newSingleThreadExecutor();

    // EventProvider which will use the executor to fire events asynchronously
    private final EventProvider<?> events = EventProvider.configure()
            .defaultStore()
            .useAsynchronousProvider().and()
            .executor(eventService)
            .create();

    // remaining code stays the same
    // ...
}
```

jeve also supports the creation of GUI events by providing EventProvider
implementations which run all listeners within the AWT Event Thread.
