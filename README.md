jeve
====

jeve is a lightweight Java 8 event dispatching framework which takes advantage
of lambda expressions and internal iteration. This avoids client code which is 
cluttered up with event delegation routines like in the following example.

```java
public class UserManager {
    
    private List<UserListener> listeners = new ArrayList<>();
    
    // ...
    
    public void addUser(user user) {
        // logic for adding a user goes here
        // ...
        // now notify our listeners
        for (UserListener listener : this.listeners) {
            listener.userAdded(user);
        }
    }
    
    public void deleteUser(user user) {
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

jeve addresses all those weaknesses by using new Java 8 features. See the 
quickstart guide below to learn how to improve event delegation.

# Quickstart
Using jeve for simple event dispatching is rather simple. It involves creating
an `EventProvider` as first step:

```java
public class UserManager {
    private final EventProvider events = EventProvider.newDefaultEventProvider();
}
```

Next, you should create an event class and a listener interface:

```java
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
public interface UserListener extends EventListener {
    public void userAdded(UserEvent e);
    
    public void userDeleted(UserEvent e);
}
```

Now, you may hook the parts together. Add methods to the `UserManager` class
to add and remove listeners. Then add methods that actually fire events to
notify registered listeners.

```java
public class UserManager {
    private final EventProvider events = EventProvider.newDefaultEventProvider();
    
    public void addUserListener(UserListener listener) {
        this.events.add(UserListener.class, listener);
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
        // ...
    }
}
```

# Advanced Topics

## Stop event delegation
Listeners are notified in order they have been registered with the 
`EventProvider`. If you want to stop the delegation of an event to further 
listeners, you may use the `Event.setHandled(boolean)` method.

```java
public class SampleUserListener implements UserListener {
    @Override
    public void userAdded(UserEvent e) {
        // do something
        // ...
        // now, stop delegation process by setting the event to be "handled".
        // No further listeners will be notified about this event.
        e.setHandled(true);
    }
    
    public void userDeleted(UserEvent e) {
        // ...
    }
}
```

## Automatically remove listeners
Sometimes it is helpful to automatically remove listeners from its parent 
`EventProvider` to allow them to be garbage collected if they are not needed 
anymore. Instead of calling `removeUserListener` yourself, you can delegate the
decision of whether a listener should be removed to the listener itself by 
implementing `OneTimeEventListener`:

```java
public class SampleUserListener implements UserListener, OneTimeEventListener {

    private boolean done = false;

    @Override
    public boolean workDone() {
        // this listener will be removed from the EventProvider it was 
        // registered at if this method returns true.
        return this.done;
    }
    
    @Override
    public void userAdded(UserEvent e) {
        // do something
        // ...
        // this listener should not be notified about further events anymore
        this.done = true;
    }
    
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
            e -> logger.error("Exception occurred during event dispatching", e));
    }
}
```

## Asynchronous event delegation
One key feature of jeve is that it hides the actual event delegation strategy 
from the actual source of the event. So if you decide that all of your 
`UserEvents` should be fired within a dedicated event thread, you simply need 
to modify the creation of the `EventProvider`:

```java
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

## Implementing own dispatching strategies
If you want to customize the process of event dispatching, you can create your
own `EventProvider` by extending `AbstractEventProvider` and overriding the
`dispatch(Class, Event, BiConsumer, ExceptionCallback)` method.