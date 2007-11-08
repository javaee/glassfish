<% 
    Object obj;
    
    // Invalidate the session to make sure this does not disturb the 
    // behavior of the following methods on PageContext:
    //     private int doGetAttributeScope(String name);
    //     private Object doFindAttribute(String name){
    //     private void doRemoveAttribute(String name){
    session.invalidate();
    
    try {
        // attr1 does not exist. findAttribute() invoked on all scopes and
        // must return a null value.
        obj = pageContext.findAttribute("attr1");
        if (obj != null) {
            out.println("ERROR: attr1 is not null on findAttribute()");            
        }  
        
        // attr1 does not exist. getAttributeScope() invoked on all scopes and
        // must return 0.
        int scope = pageContext.getAttributesScope("attr1");
       if (scope != 0) {
            out.println("ERROR: scope is not 0 on getAttributesScope()");            
        }                
        
        // set attr2 in application scope.
        // Make sure it gets removed properly.
        pageContext.setAttribute("attr2", "attr2", PageContext.APPLICATION_SCOPE);
        pageContext.removeAttribute("attr2");
        obj = pageContext.findAttribute("attr2");
        if (obj != null) {
            out.println("ERROR: attr2 is not null");            
        }        

        // Success!
        out.println("SUCCESS");
        
    } catch (IllegalStateException ex) {
        out.println("ERROR: invalidated session throws IllegalStateException when trying to find/remove attribute");
    }
%>