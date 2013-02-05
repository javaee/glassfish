This test suite is sopposed to assert a non-jta datasource is used by 
unsynchronized persistence context when haven't joined a transaction.

From JSR388, Chapter 7.6.1 Persistence Context Synchronization Type:
It is recommended that a non-JTA datasource be specified for use by the persistence provider
for a persistence context of type SynchronizationType.UNSYNCHRONIZED that has not been
joined to a JTA transaction in order to alleviate the risk of integrating uncommitted changes 
into the persistence context in the event that the transaction is later rolled back.

Two test cases are involed:
1. In a transaction, obtain a connection and update an entity to database, and then use 
an unsynchronized PC to find this entity and assert the entity contains the old value.

2. Rollback the transaction, assert the unsynchronized PC is not cleared. 


