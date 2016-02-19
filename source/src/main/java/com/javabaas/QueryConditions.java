package com.javabaas;

import com.alibaba.fastjson.JSONObject;
import com.javabaas.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryConditions {

    Map<String, List<QueryOperation>> where = new HashMap<>();
    private List<String> include = new LinkedList<>();
    private Set<String> selectedKeys;
    private int limit;
    private boolean trace;
    private int skip;
    private String order;
    private Map<String , Object> orderMap = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();

    public QueryConditions() {
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSkip() {
        return this.skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public String getOrder() {
        return this.order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public List<String> getInclude() {
        return this.include;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public Set<String> getSelectedKeys() {
        return this.selectedKeys;
    }

    public void setSelectedKeys(Set<String> selectedKeys) {
        this.selectedKeys = selectedKeys;
    }

    public Map<String, List<QueryOperation>> getWhere() {
        return this.where;
    }

    public void setWhere(Map<String, List<QueryOperation>> where) {
        this.where = where;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public boolean isTrace() {
        return this.trace;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public void addAscendingOrder(String key) {
        if(Utils.isBlankString(this.order)) {
            this.orderByAscending(key);
        } else {
            this.order = String.format("%s,%s", new Object[]{this.order, key});
            orderMap.put(key , 1);
        }
    }

    public void orderByAscending(String key) {
        this.order = String.format("%s", new Object[]{key});
        orderMap.put(key , 1);
    }

    public void addDescendingOrder(String key) {
        if(Utils.isBlankString(this.order)) {
            this.orderByDescending(key);
        } else {
            this.order = String.format("%s,-%s", new Object[]{this.order, key});
            orderMap.put(key , -1);
        }
    }

    public void orderByDescending(String key) {
        this.order = String.format("-%s", new Object[]{key});
        orderMap.put(key , -1);
    }

    public void include(String key) {
        this.include.add(key);
    }

    public void selectKeys(Collection<String> keys) {
        if(this.selectedKeys == null) {
            this.selectedKeys = new HashSet();
        }

        this.selectedKeys.addAll(keys);
    }

    public Map<String, Object> compileWhereOperationMap() {
        HashMap result = new HashMap();
        Iterator i$ = this.where.entrySet().iterator();

        while(true) {
            label58:
            while(i$.hasNext()) {
                Map.Entry entry = (Map.Entry)i$.next();
                List ops = (List)entry.getValue();
                String key = (String)entry.getKey();
                if(key.equals("$or")) {
                    ArrayList iterator1 = new ArrayList();
                    Iterator opList3 = ops.iterator();

                    while(opList3.hasNext()) {
                        QueryOperation opMap1 = (QueryOperation)opList3.next();
                        iterator1.add(opMap1.toResult());
                    }

                    List opList2 = (List)result.get("$or");
                    if(opList2 != null) {
                        opList2.addAll(iterator1);
                    } else {
                        result.put("$or", iterator1);
                    }
                } else {
                    HashMap opMap;
                    boolean hasEqual;
                    Iterator existsAnd;
                    ArrayList opList1;
                    switch(ops.size()) {
                        case 0:
                            continue;
                        case 1:
                            Iterator iterator = ops.iterator();

                            while(true) {
                                if(!iterator.hasNext()) {
                                    continue label58;
                                }

                                QueryOperation opList = (QueryOperation)iterator.next();
                                result.put(key, opList.toResult());
                            }
                        default:
                            opList1 = new ArrayList();
                            opMap = new HashMap();
                            hasEqual = false;
                            existsAnd = ops.iterator();
                    }

                    while(existsAnd.hasNext()) {
                        QueryOperation op = (QueryOperation)existsAnd.next();
                        opList1.add(op.toResult(key));
                        if("__eq".equals(op.op)) {
                            hasEqual = true;
                        }

                        if(!hasEqual) {
                            opMap.putAll((Map)op.toResult());
                        }
                    }

                    if(hasEqual) {
                        List existsAnd1 = (List)result.get("$and");
                        if(existsAnd1 != null) {
                            existsAnd1.addAll(opList1);
                        } else {
                            result.put("$and", opList1);
                        }
                    } else {
                        result.put(key, opMap);
                    }
                }
            }

            return result;
        }
    }

    public void addWhereItem(QueryOperation op) {
        List ops = (List)this.where.get(op.key);
        if(ops == null) {
            ops = new LinkedList();
            this.where.put(op.key, ops);
        }

        this.removeDuplications(op, ops);
        ops.add(op);
    }

    public void addWhereItem(String key, String op, Object value) {
        this.addWhereItem(new QueryOperation(key, op, value));
    }

    private void removeDuplications(QueryOperation op, List<QueryOperation> ops) {
        Iterator it = ops.iterator();

        while(it.hasNext()) {
            QueryOperation o = (QueryOperation)it.next();
            if(o.sameOp(op)) {
                it.remove();
            }
        }

    }

    public void addOrItems(QueryOperation op) {
        List ops = (List)this.where.get("$or");
        if(ops == null) {
            ops = new LinkedList();
            this.where.put("$or", ops);
        }

        Iterator it = ops.iterator();

        while(it.hasNext()) {
            QueryOperation o = (QueryOperation)it.next();
            if(o.equals(op)) {
                it.remove();
            }
        }

        ops.add(op);
    }

    public void whereGreaterThanOrEqualTo(String key, Object value) {
        this.addWhereItem(new QueryOperation(key, "$gte", value));
    }

    public void whereContainedIn(String key, Collection<? extends Object> values) {
        this.addWhereItem(key, "$in", values);
    }

    public void whereExists(String key) {
        this.addWhereItem(key, "$exists", Boolean.valueOf(true));
    }

    public void whereGreaterThan(String key, Object value) {
        this.addWhereItem(key, "$gt", value);
    }

    public void whereLessThan(String key, Object value) {
        this.addWhereItem(key, "$lt", value);
    }

    public void whereLessThanOrEqualTo(String key, Object value) {
        this.addWhereItem(key, "$lte", value);
    }

    public void whereMatches(String key, String regex) {
        this.addWhereItem(key, "$regex", regex);
    }

    public void whereMatches(String key, String regex, String modifiers) {
        this.addWhereItem(key, "$regex", regex);
        this.addWhereItem(key, "$options", modifiers);
    }

    public void whereNotContainedIn(String key, Collection<? extends Object> values) {
        this.addWhereItem(key, "$nin", values);
    }

    public void whereNotEqualTo(String key, Object value) {
        this.addWhereItem(key, "$ne", value);
    }

    public void whereEqualTo(String key, Object value) {
        if(value instanceof JBObject) {
            this.addWhereItem(key , "__eq", Utils.mapFromPointerObject((JBObject) value));
        } else {
            this.addWhereItem(key, "__eq", value);
        }
    }
    public void whereStartsWith(String key, String prefix) {
        this.whereMatches(key, String.format("^%s.*", new Object[]{prefix}));
    }

    public void whereEndsWith(String key, String suffix) {
        this.whereMatches(key, String.format(".*%s$", new Object[]{suffix}));
    }

    public void whereContains(String key, String substring) {
        String regex = String.format(".*%s.*", new Object[]{substring});
        this.whereMatches(key, regex);
    }

    public void whereSizeEqual(String key, int size) {
        this.addWhereItem(key, "$size", Integer.valueOf(size));
    }

    public void whereContainsAll(String key, Collection<?> values) {
        this.addWhereItem(key, "$all", values);
    }

    public void whereDoesNotExist(String key) {
        this.addWhereItem(key, "$exists", Boolean.valueOf(false));
    }


    public Map<String, String> assembleParameters() {
        if(this.where.keySet().size() > 0) {
            this.parameters.put("where", Utils.restfulServerData(this.compileWhereOperationMap()));
        }

        if(this.limit > 0) {
            this.parameters.put("limit", Integer.toString(this.limit));
        }

        if(this.skip > 0) {
            this.parameters.put("skip", Integer.toString(this.skip));
        }

        if(!Utils.isBlankString(this.order)) {
            this.parameters.put("order", new JSONObject(orderMap).toString());
        }

        String keys;
        if(!Utils.isEmptyList(this.include)) {
            keys = Utils.joinCollection(this.include, ",");
            this.parameters.put("include", keys);
        }

        if(this.selectedKeys != null && this.selectedKeys.size() > 0) {
            keys = Utils.joinCollection(this.selectedKeys, ",");
            this.parameters.put("keys", keys);
        }

        return this.parameters;
    }
}
