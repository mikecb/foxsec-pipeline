package com.mozilla.secops.state;

import net.spy.memcached.MemcachedClient;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

public class MemcachedStateInterface implements StateInterface {
    private String memcachedHost;
    private MemcachedClient memclient;

    public String getObject(String s) throws StateException {
        return (String)memclient.get(s);
    }

    public void saveObject(String s, String v) throws StateException {
        try {
            memclient.set(s, 0, v);
        } catch (IllegalArgumentException exc) {
            throw new StateException(exc.getMessage());
        }
    }

    public void done() {
        memclient.shutdown();
    }

    public void initialize() throws StateException {
        try {
            memclient = new MemcachedClient(new InetSocketAddress(memcachedHost, 11211));
        } catch (IOException exc) {
            throw new StateException(exc.getMessage());
        }
    }

    public MemcachedStateInterface(String host) {
        memcachedHost = host;
    }
}
