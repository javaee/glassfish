This test suite is supposed to assert an EJBException should be thrown when creating
a Stateful Session bean bound with an extended PC in an transaction and a different 
PC is already assosiated with the transaction.

From JSR338, Chapter 7.6.4.1 Requirements for Persistence Context Propagation:
If a component is called and the JTA transaction is propagated into that component:
If the component is a stateful session bean to which an extended persistence context has been
bound and there is a different persistence context associated with the JTA transaction, an
EJBException is thrown by the container.

Two test cases are involed:
1. A SFSBean with extended unsynchronized PC create SFSBean with synchronized PC.
Assert a EJBException should be thrown.
2. A SFSBean with synchronized PC create SFSBean with unsynchronized PC. Assert a 
EJBException should be thrown.

