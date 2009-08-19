package com.sun.enterprise.v3.services.impl;

import java.util.List;

import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.tcp.Adapter;

/**
 * Class represents context-root associated information
 */
public class ContextRootInfo {
    protected Adapter adapter;
    protected Object container;
    protected List<ProtocolFilter> protocolFilters;

    public ContextRootInfo() {
    }

    public ContextRootInfo(final Adapter adapter, final Object container, final List<ProtocolFilter> protocolFilters) {
        this.adapter = adapter;
        this.container = container;
        this.protocolFilters = protocolFilters;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public void setAdapter(final Adapter adapter) {
        this.adapter = adapter;
    }

    public Object getContainer() {
        return container;
    }

    public void setContainer(final Object container) {
        this.container = container;
    }

    public List<ProtocolFilter> getProtocolFilters() {
        return protocolFilters;
    }

    public void setProtocolFilters(final List<ProtocolFilter> protocolFilters) {
        this.protocolFilters = protocolFilters;
    }
}
