package com.javabaas;

import android.text.TextUtils;

import java.util.HashMap;

public class QueryOperation {

    public static final String EQUAL_OP = "__eq";
    public static final String OR_OP = "$or";
    String key;
    Object value;
    String op;

    public QueryOperation(String key, String op, Object value) {
        this.key = key;
        this.op = op;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public Object getValue() {
        return this.value;
    }

    public String getOp() {
        return this.op;
    }

    public Object toResult() {
        if(this.op != null && !this.op.equals("__eq") && !this.op.equals("$or")) {
            HashMap map = new HashMap();
            map.put(this.op, this.value);
            return map;
        } else {
            return this.value;
        }
    }

    public Object toResult(String key) {
        HashMap map = new HashMap();
        map.put(key, this.toResult());
        return map;
    }

    public boolean sameOp(QueryOperation other) {
        return TextUtils.equals(this.key, other.key) && TextUtils.equals(this.op, other.op);
    }

    public int hashCode() {
        boolean prime = true;
        byte result = 1;
        int result1 = 31 * result + (this.key == null?0:this.key.hashCode());
        result1 = 31 * result1 + (this.op == null?0:this.op.hashCode());
        result1 = 31 * result1 + (this.value == null?0:this.value.hashCode());
        return result1;
    }

    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(obj == null) {
            return false;
        } else if(this.getClass() != obj.getClass()) {
            return false;
        } else {
            QueryOperation other = (QueryOperation)obj;
            if(this.key == null) {
                if(other.key != null) {
                    return false;
                }
            } else if(!this.key.equals(other.key)) {
                return false;
            }

            if(this.op == null) {
                if(other.op != null) {
                    return false;
                }
            } else if(!this.op.equals(other.op)) {
                return false;
            }

            if(this.value == null) {
                if(other.value != null) {
                    return false;
                }
            } else if(!this.value.equals(other.value)) {
                return false;
            }

            return true;
        }
    }
}
