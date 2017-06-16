This test suite is supposed to assert that when roll back happens, if the unsynchronized
persistence context have already joined the transaction, the persistence context will be cleared.

From SPEC JSR338, chepter 7.6.1 Persistence Context Synchronization Type:
If a persistence context of type SynchronizationType.UNSYNCHRONIZED has been joined to
the JTA transaction, transaction rollback will cause the persistence context to be cleared and all
pre-existing managed and removed instances to become detached. 

One test case is involed:
1. Persist a entity to the database, invoke joinTransaction method, and then roll back the transaction
assert the PC doesn't contain the entity any longer.

