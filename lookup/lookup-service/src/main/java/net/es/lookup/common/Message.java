package net.es.lookup.common;

import org.apache.commons.lang.math.LongRange;

import java.rmi.MarshalledObject;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class  Message {

    public static final String ACCESS_POINT = "access-point";
    public static final String CLIENT_UUID = "client-uuid";
    public static final String TTL = "ttl";
    public static final String SERVICE_NAME = "name";
    public static final String SERVICE_DOMAIN = "domain";
    public static final String SERVICE_TYPE = "service-type";
    public static final String SERVICE_URI = "uri";
    public static final String QUERY_OPERATOR = "operator";
    public static final String EXPIRES = "expires";
    public static final String DEFAULT_OPERATOR = "any";
    public static final String OPERATOR_ALL = "all";
    public static final String OPERATOR_ANY = "any";

    private final Map<String,Object> keyValues;
    protected int status = 0;
    private int error = 0;
    private String errorMessage = "";

    public Message() {
        this.keyValues = new HashMap<String, Object>();
    }

    public Message(Map<String,Object> map) {
        this.keyValues = map;
    }

    public synchronized int getStatus() {
        return this.status;
    }

    public synchronized int setStatus(int status) {
        int old = this.status;
        this.status = status;
        return old;
    }

    public final Map getMap() {
        return this.keyValues;
    }

    public synchronized void add (String key, Object value) throws DuplicateKeyException {
        if (this.keyValues.containsKey(key)) {
            throw new DuplicateKeyException("Duplicate key: " + key);
        }
        this.keyValues.put(key, value);
    }

    public String getURI() {
        return (String) this.getMap().get(Message.SERVICE_URI);
    }

    public long getTTL() {
        Long res = (Long) this.getMap().get(Message.TTL);
        if (res == null) {
            return -1;
        }
        return res;
    }

    public List<String> getServiceType() {
        return  (List<String>) this.getMap().get(Message.SERVICE_TYPE);
    }

    public List<String> getAccessPoint() {
        return  (List<String>) this.getMap().get(Message.ACCESS_POINT);
    }

    public List<String> getServiceName() {
        return  (List<String>) this.getMap().get(Message.SERVICE_NAME);
    }

    public List<String> getServiceDomain() {
        return  (List<String>) this.getMap().get(Message.SERVICE_DOMAIN);
    }
    
    public List<String> getClientUUID() {
        return  (List<String>) this.getMap().get(Message.CLIENT_UUID);
    }

    public List<String> getOperator() {
        return (List<String>) this.getMap().get(Message.QUERY_OPERATOR);
    }

    public synchronized void setError (int error) {
        this.error = error;
    }

    public synchronized int getError () {
        return this.error;
    }

    public synchronized void setErrorMessage (String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public synchronized String getErrorMessage () {
        return this.errorMessage;
    }
}