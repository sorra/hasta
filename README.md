# Hasta: a library to copy, join and compose beans

## hasta-util
**BeanCopy** (details in BeanCopyTest)

bean definitions:

```java
class User {
  String name;
  List<User> underHands;
}
class UserView {
  String name;
  Collection<UserView> underHands;
}
```
single copy:

```java
User user = queryFromSomewhere();
UserView userView = BeanCopy.copy(user, UserView.class);
```
collection copy:

```java
Set<User> users = queryFromSomewhere();
Collection<UserView> userViews = new ArrayList<>();
BeanCopy.copy(users, userViews, User.class, UserView.class);
```
map copy

```java
Map<String, User> usersMap = queryFromSomewhere();
Map<String, UserView> userViewsMap = new HashMap<>();
BeanCopy.copy(usersMap, userViewsMap, User.class, UserView.class);
```
converters:

```java
// Converts enum to Integer
ConverterRegistry.put(Gender.class.getName(), Integer.class.getName(), new Converter() {
      @Override
      public Object convert(Object from) {
        return ((Enum) from).ordinal();
      }
    });
```
Early check:

```java
// Use in unit tests to early check the copyability between the two classes
BeanCopierRegistry.prepare(User.class, UserView.class);
```

**Join** (details in JoinTest)

joins two collections on the key properties of beans:  

```java
// The code snippet is Java 8, but you can use Java 7+
Join.loopJoin(users, userAddresses, User::getId, UserAddress::getUserId,
    (id, user, address) -> new UserView(user, address));

Join.hashJoin(...) // similar to the above

Join.mapsJoin(usersMap, userAddressesMap,
    (id, user, address) -> new UserView(user, address))
```

## hasta-core

On-going
