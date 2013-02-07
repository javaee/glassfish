MixedScoped test cases contain a number of use cases which demonstrate how the scope proposed in Injection of JMSContext objects - Proposals (version 4) would appear to users. Each use case is followed by an analysis. Please refer the wiki page http://java.net/projects/jms-spec/pages/JMSContextScopeProposalsv4p1.

jmsContextDefaultInjection test scenarios:Test whether inject1 and inject2 using identical annotations or not.
Inject1
	@Inject
	private JMSContext jmsContext;
Inject2
	@Inject
	@JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
	private JMSContext jmsContext;

