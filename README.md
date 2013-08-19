scala-riak-session-management
=============================

What is this ?
--------------
A simple library for server side session backed by riak and/or filesystem folder.
A sample illustrating its use in a [Play](http://www.playframework.com) application.

How it works ?
---------------

Just create a Riak Json formatted session handler

```val sessionManager =  new RiakBackend("127.0.0.1", 8098, "SESSION", 20)  with JSONConverter[Session]

                                                                            with SessionHandler```

or create a File Binary formatted session handler
```val sessionManager = new FileBackend(Files.createTempDir(), "SESSION", 20) with BinaryConverter[Session] 

                                                                              with SessionHandler```

Add a new session

`val session = sessionManager.addNewSession`

Set Attributes

`session.put("key", "value")`

Get Attribute

`val value : Any = session("key")`


Is it extensible ?
-------------------

Adding a new formatting require to implement the trait `Converter[A]` and adding a different backend (SQL Based ...) require to implement the trait `Backend[A]`.



