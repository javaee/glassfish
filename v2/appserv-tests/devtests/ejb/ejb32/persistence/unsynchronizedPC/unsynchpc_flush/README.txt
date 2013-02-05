This test suite is suppose to assert the flush method should not be invoked before 
an unsynchronized PC joins a transaction, and instead, it could be invoked after 
join the transaction.

From JSR338, Chapter 7.6.1 Persistence Context Synchronization Type:
A persistence context of type SynchronizationType.UNSYNCHRONIZED must not be flushed to
the database unless it is joined to a transaction. 
After the persistence context has been joined to a transaction, changes in a persistence 
context can be flushed to the database either explicitly by the application or by the provider. 
If the flush method is not explicitly invoked, the persistence provider may defer flushing 
until commit time depending on the operations invoked and the flush mode setting in effect.

Three test cases are involed:
1. Invoke flush method before a unsynchronized PC join a transaction, assert 
an TransactionRequiredException should be thrown.
2. Invoke flush method after join a transaction, assert no exception should be thrown.
3. Don't explicitly invoke flush method, after a transaction is commited, assert the data is
automatically updated to database. 
