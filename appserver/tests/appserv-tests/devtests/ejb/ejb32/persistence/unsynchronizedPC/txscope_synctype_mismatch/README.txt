This test suite is supposed to assert when the persistence propagation happens for example invokation 
cross multiple components, the unsynchronized persistence context should not propagate to synchronized.

From JSR388, Chapter 7.6.4.1 Requirements for Persistence Context Propagation:
If a component is called and the JTA transaction is propagated into that component,
If there is a persistence context of type SynchronizationType.UNSYNCHRONIZED
associated with the JTA transaction and the target component specifies a persistence context of
type SynchronizationType.SYNCHRONIZED, an EJBException is thrown by the container.

Four test cases are involed in this suite, which are:
1. SFSBean with unsynchronized PC invoke SLSBean with synchronized PC. Assert EJBException should be thrown when invokation
2. SLSBean with unsynchronized PC invoke SLSBean with synchronized PC. Assert EJBException should be thrown when invokation
3. SLSBean with synchronized PC invoke SLSBean with unsynchronized PC. Assert no excetion should be thrown
4. SLSBean with synchronized PC invoke SFSBean with unsynchronized PC. Assert no excetion should be thrown


