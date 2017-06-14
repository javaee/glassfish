This test suite is supposed to assert the dis-allowed and allowed methods 
would work properly when invoked in an unsynchronized persistence context if the PC 
haven't join to a transaction.

From SPEC JSR338, chepter 7.6.1 Persistence Context Synchronization Type:
The application's use of queries with pessimistic locks, bulk update or delete queries,
etc. result in the provider throwing the TransactionRequiredException. 

Disallowed methods - queries with pessimistic locks,bulk update or delete queries. 
Assert invokation of these method results in the provider throwing the TransactionRequiredException.

Allowed methods - persist, merge, remove and referesh. 
Assert invokation of these method result in no exceptions thrown out.

