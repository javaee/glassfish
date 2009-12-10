<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
    <sun:button id="def" rendered="#{edit}" primary="#{false}"  text="$resource{i18n.button.LoadDefaults}" >
        <!command
            getDefaultPhysicalDestinationValues( orig="#{pageSession.valueMap}"  map="#{pageSession.valueMap}" )
        />
    </sun:button>
    <!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
     <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.Save}"
            onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            createPhysicalDestination(name="#{name}", type="#{type}", attributes="#{pageSession.valueMap}");
            gf.redirect(page="#{pageSession.parentPage}");
        />
    </sun:button>
     <sun:button id="updateButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
            onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            updatePhysicalDestination(name="#{pageSession.destName}", type="#{pageSession.destType}", attributes="#{pageSession.valueMap}");
            gf.redirect(page="#{pageSession.parentPage}");
        />
    </sun:button>
    <sun:button id="cancelButton" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
        <!command
            gf.redirect(page="#{pageSession.parentPage}");
        />
  </sun:button>

</sun:panelGroup>
</facet>
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="NameTextProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
            <h:outputText id="NameStatic" rendered="#{edit}" value="#{pageSession.destName}"/>
            <h:inputHidden rendered="#{edit}" value="#{pageSession.destName}" />
            <sun:textField id="NameText" rendered="#{!edit}" styleClass="required" text="#{name}" columns="$int{55}" required="#{true}" />
        </sun:property>
        <sun:property id="typeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.type}">
            <h:outputText id="TypeStatic" rendered="#{edit}" value="#{pageSession.destType}"/>
            <h:inputHidden rendered="#{edit}" value="#{pageSession.destType}" />
            <sun:dropDown id="type" rendered="#{!edit}" labels={"javax.jms.Topic" "javax.jms.Queue"} values={"t" "q"} selected="#{type}" />
        </sun:property>

        <sun:property labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.MaxNumMsgs}" helpText="$resource{i18njms.jmsPhysDestinations.MaxNumMsgsHelp}" >
            <sun:textField id="maxNumMsgs" text="#{pageSession.valueMap['MaxNumMsgs']}" columns="$int{6}"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.MaxBytesPerMsg}" helpText="$resource{i18njms.jmsPhysDestinations.MaxBytesPerMsgHelp}" >
            <sun:textField id="maxNumMsgs" text="#{pageSession.valueMap['MaxBytesPerMsg']}" columns="$int{6}"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.MaxTotalMsgBytes}" helpText="$resource{i18njms.jmsPhysDestinations.MaxTotalMsgBytesHelp}" >
            <sun:textField id="maxNumMsgs" text="#{pageSession.valueMap['MaxTotalMsgBytes']}" columns="$int{6}"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.LimitBehavior}" helpText="$resource{i18njms.jmsPhysDestinations.LimitBehaviorHelp}" >
            <sun:dropDown id="Type" values={"REJECT_NEWEST", "FLOW_CONTROL","REMOVE_OLDEST","REMOVE_LOW_PRIORITY"}
                          labels={"Reject newest messages", "Slow down producers", "Throw out oldest", "Throw out lowest-priority messages"}
                          selected="#{pageSession.valueMap['LimitBehavior']}"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.MaxNumProducers}" helpText="$resource{i18njms.jmsPhysDestinations.MaxNumProducersHelp}" >
            <sun:textField id="maxNumMsgs" text="#{pageSession.valueMap['MaxNumProducers']}" columns="$int{6}"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" rendered="#{pageSesssion.destType != 'topic'" label="$resource{i18njms.jmsPhysDestinations.MaxNumActiveConsumers}" helpText="$resource{i18njms.jmsPhysDestinations.MaxNumActiveConsumersHelp}" >
            <sun:textField id="maxNumActiveConsumers" text="#{pageSession.valueMap['MaxNumActiveConsumers']}" columns="$int{6}"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" rendered="#{pageSesssion.destType != 'topic'" label="$resource{i18njms.jmsPhysDestinations.MaxNumBackupConsumers}" helpText="$resource{i18njms.jmsPhysDestinations.MaxNumBackupConsumersHelp}" >
            <sun:textField id="maxNumBackupConsumers" text="#{pageSession.valueMap['MaxNumBackupConsumers']}" columns="$int{6}"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.ConsumerFlowLimit}" helpText="$resource{i18njms.jmsPhysDestinations.ConsumerFlowLimitHelp}" >
            <sun:textField id="consumerFlowLimit" text="#{pageSession.valueMap['ConsumerFlowLimit']}" columns="$int{6}"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" rendered="#{pageSesssion.destType != 'topic'" label="$resource{i18njms.jmsPhysDestinations.LocalDeliveryPreferred}" helpText="$resource{i18njms.jmsPhysDestinations.LocalDeliveryPreferredHelp}" >
            <sun:dropDown id="localDeliveryPreferred" values={"true", "false"} labels={"True", "False"} selected="#{pageSession.valueMap['LocalDeliveryPreferred']}" selectedValue="true"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.UseDMQ}" helpText="$resource{i18njms.jmsPhysDestinations.UseDMQHelp}" >
            <sun:dropDown id="useDmq" values={"true", "false"} labels={"True", "False"} selected="#{pageSession.valueMap['UseDMQ']}" selectedValue="true"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.ValidateXMLSchemaEnabled}" helpText="$resource{i18njms.jmsPhysDestinations.ValidateXMLSchemaEnabledHelp}" >
            <sun:dropDown id="validateXMLSchemaEnabled" values={"true", "false"} labels={"True", "False"} selected="#{pageSession.valueMap['ValidateXMLSchemaEnabled']}"/>
        </sun:property>
        <sun:property labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.jmsPhysDestinations.XMLSchemaURIList}" helpText="$resource{i18njms.jmsPhysDestinations.XMLSchemaURIListHelp}" >
            <sun:textField id="xmlSchemaURIList" text="#{pageSession.valueMap['XMLSchemaURIList']}" columns="$int{55}"/>
        </sun:property>
    </sun:propertySheetSection>
</sun:propertySheet>