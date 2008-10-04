package com.sun.enterprise.resource;

import com.sun.enterprise.resource.allocator.ResourceAllocator;


public class AssocWithThreadResourceHandle extends ResourceHandle{

    private boolean associated_ = false;
    private long threadId_;
    private boolean dirty_;

    public AssocWithThreadResourceHandle(Object resource, ResourceSpec spec, ResourceAllocator alloc,
                                         ClientSecurityInfo info) {
        super(resource, spec, alloc, info);
    }

    public boolean isDirty() {
        return dirty_;
    }

    public void setDirty() {
        dirty_ = true;
    }

    public boolean isAssociated() {
        return associated_;
    }

    public void setAssociated( boolean flag ) {
        associated_ = flag;
    }

    public long getThreadId() {
        return threadId_;
    }

    public void setThreadId( long threadId ) {
        threadId_ = threadId;
    }
}
