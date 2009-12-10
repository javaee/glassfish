<sun:propertySheet id="propertySheet">
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">
        <event>
        <!beforeCreate
            getRequestValue(key="name" value=>$page{jndiName});
            setAttribute(key="enabled" value="#{true}");
        />
        </event>
        <sun:property id="jndiProp" rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}" helpText="$resource{i18n.common.jndiNameHelp}" >
            <sun:textField id="jndi" columns="$int{55}" text="#{pageSession.valueMap['Name']}" styleClass="required" required="#{true}" />
        </sun:property>

        <sun:property rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}" >
            <h:outputText value="#{pageSession.valueMap['Name']}" />
        </sun:property>

        <sun:property id="nameProp" labelAlign="left" noWrap="true" overlapLabel="false" label="$resource{i18njms.jmsDestination.destName}" helpText="$resource{i18njms.jmsDestination.destNameHelp}">
            <sun:textField id="name" columns="55" text="#{pageSession.physDestName}" styleClass="required" required="true" />
        </sun:property>

        <sun:property id="resTypeProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resType}"  >
            <sun:dropDown id="resType" selected="#{pageSession.valueMap['ResType']}" labels={"javax.jms.Topic"  "javax.jms.Queue"}  required="#{true}"/>
        </sun:property>

        <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
            <sun:textField id="desc" columns="$int{55}" text="#{pageSession.valueMap['Description']}" />
        </sun:property>

        <sun:property id="statusProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox id="cb" label="$resource{i18n.common.Enabled}" immediate="#{true}" selected="#{pageSession.valueMap2['Enabled']}" selectedValue="true"/>
        </sun:property>
        
        "<br /><br />
    </sun:propertySheetSection>
</sun:propertySheet>
