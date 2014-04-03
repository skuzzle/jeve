jeve
====

Lightweight Java 8 Event Dispatching Framework

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