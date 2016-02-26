package com.javabaas;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.javabaas.callback.CountCallback;
import com.javabaas.callback.DeleteCallback;
import com.javabaas.callback.FindCallback;
import com.javabaas.exception.JBException;
import com.javabaas.util.Utils;

public class JBQuery<T extends JBObject> {

    private Class<T> clazz;
    private String className;
    private JBQuery.CachePolicy cachePolicy = CachePolicy.NETWORK_ONLY;
    private String queryPath;
    private String externalQueryPath;
    QueryConditions conditions;
    private IObjectManager manager;

    public JBQuery(String theClassName) {
        this(theClassName, (Class) null);
        this.className = theClassName;
    }

    public static JBQuery<JBObject> getInstance(String theClassName) {
        return new JBQuery<>(theClassName, JBObject.class);
    }

    public JBQuery(String theClassName, Class<T> clazz) {
        this.cachePolicy = JBQuery.CachePolicy.IGNORE_CACHE;
        Utils.checkClassName(theClassName);
        this.className = theClassName;
        this.clazz = clazz;
        this.conditions = new QueryConditions();
        manager = JBCloud.getObjectManager(theClassName);
    }

    Class<T> getClazz() {
        return this.clazz;
    }

    void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    List<String> getInclude() {
        return this.conditions.getInclude();
    }

    void setInclude(List<String> include) {
        this.conditions.setInclude(include);
    }

    Set<String> getSelectedKeys() {
        return this.conditions.getSelectedKeys();
    }

    void setSelectedKeys(Set<String> selectedKeys) {
        this.conditions.setSelectedKeys(selectedKeys);
    }

    Map<String, String> getParameters() {
        return this.conditions.getParameters();
    }

    void setParameters(Map<String, String> parameters) {
        this.conditions.setParameters(parameters);
    }

    String getQueryPath() {
        return this.queryPath;
    }

    void setQueryPath(String queryPath) {
        this.queryPath = queryPath;
    }

    String getExternalQueryPath() {
        return this.externalQueryPath;
    }

    void setExternalQueryPath(String path) {
        this.externalQueryPath = path;
    }

    static String getTag() {
        return "com.parse.JBQuery";
    }

    Map<String, List<QueryOperation>> getWhere() {
        return this.conditions.getWhere();
    }

    public String getClassName() {
        return this.className;
    }

    public JBQuery<T> setClassName(String className) {
        this.className = className;
        return this;
    }

    public JBQuery.CachePolicy getCachePolicy() {
        return this.cachePolicy;
    }

    public JBQuery<T> setCachePolicy(JBQuery.CachePolicy cachePolicy) {
        this.cachePolicy = cachePolicy;
        return this;
    }

    public JBQuery.CachePolicy getPolicy() {
        return this.cachePolicy;
    }

    public JBQuery<T> setPolicy(JBQuery.CachePolicy policy) {
        this.cachePolicy = policy;
        return this;
    }

    public boolean isTrace() {
        return this.conditions.isTrace();
    }

    public JBQuery<T> setTrace(boolean trace) {
        this.conditions.setTrace(trace);
        return this;
    }

    public int getLimit() {
        return this.conditions.getLimit();
    }

    public JBQuery<T> setLimit(int limit) {
        this.conditions.setLimit(limit);
        return this;
    }

    public JBQuery<T> limit(int limit) {
        this.setLimit(limit);
        return this;
    }

    public JBQuery<T> skip(int skip) {
        this.setSkip(skip);
        return this;
    }

    public int getSkip() {
        return this.conditions.getSkip();
    }

    public JBQuery<T> setSkip(int skip) {
        this.conditions.setSkip(skip);
        return this;
    }

    public String getOrder() {
        return this.conditions.getOrder();
    }

    public JBQuery<T> setOrder(String order) {
        this.conditions.setOrder(order);
        return this;
    }

    public JBQuery<T> order(String order) {
        this.setOrder(order);
        return this;
    }

    public JBQuery<T> addAscendingOrder(String key) {
        this.conditions.addAscendingOrder(key);
        return this;
    }

    public JBQuery<T> addDescendingOrder(String key) {
        this.conditions.addDescendingOrder(key);
        return this;
    }

    public JBQuery<T> include(String key) {
        this.conditions.include(key);
        return this;
    }

    public JBQuery<T> selectKeys(Collection<String> keys) {
        this.conditions.selectKeys(keys);
        return this;
    }

    public JBQuery<T> orderByAscending(String key) {
        this.conditions.orderByAscending(key);
        return this;
    }

    public JBQuery<T> orderByDescending(String key) {
        this.conditions.orderByDescending(key);
        return this;
    }

    public JBQuery<T> whereContainedIn(String key, Collection<? extends Object> values) {
        this.conditions.whereContainedIn(key, values);
        return this;
    }

    public JBQuery<T> whereContains(String key, String substring) {
        this.conditions.whereContains(key, substring);
        return this;
    }

    public JBQuery<T> whereSizeEqual(String key, int size) {
        this.conditions.whereSizeEqual(key, size);
        return this;
    }

    public JBQuery<T> whereContainsAll(String key, Collection<?> values) {
        this.conditions.whereContainsAll(key, values);
        return this;
    }

    public JBQuery<T> whereDoesNotExist(String key) {
        this.conditions.whereDoesNotExist(key);
        return this;
    }

    public JBQuery<T> whereEndsWith(String key, String suffix) {
        this.conditions.whereEndsWith(key, suffix);
        return this;
    }

    public JBQuery<T> whereEqualTo(String key, Object value) {
        this.conditions.whereEqualTo(key, value);
        return this;
    }

    private JBQuery<T> addWhereItem(QueryOperation op) {
        this.conditions.addWhereItem(op);
        return this;
    }

    private JBQuery<T> addOrItems(QueryOperation op) {
        this.conditions.addOrItems(op);
        return this;
    }

    protected JBQuery<T> addWhereItem(String key, String op, Object value) {
        this.conditions.addWhereItem(key, op, value);
        return this;
    }

    public JBQuery<T> whereExists(String key) {
        this.conditions.whereExists(key);
        return this;
    }

    public JBQuery<T> whereGreaterThan(String key, Object value) {
        this.conditions.whereGreaterThan(key, value);
        return this;
    }

    public JBQuery<T> whereGreaterThanOrEqualTo(String key, Object value) {
        this.conditions.whereGreaterThanOrEqualTo(key, value);
        return this;
    }

    public JBQuery<T> whereLessThan(String key, Object value) {
        this.conditions.whereLessThan(key, value);
        return this;
    }

    public JBQuery<T> whereLessThanOrEqualTo(String key, Object value) {
        this.conditions.whereLessThanOrEqualTo(key, value);
        return this;
    }

    public JBQuery<T> whereMatches(String key, String regex) {
        this.conditions.whereMatches(key, regex);
        return this;
    }

    public JBQuery<T> whereMatches(String key, String regex, String modifiers) {
        this.conditions.whereMatches(key, regex, modifiers);
        return this;
    }

    public JBQuery<T> whereNotContainedIn(String key, Collection<? extends Object> values) {
        this.conditions.whereNotContainedIn(key, values);
        return this;
    }

    public JBQuery<T> whereNotEqualTo(String key, Object value) {
        this.conditions.whereNotEqualTo(key, value);
        return this;
    }

    public JBQuery<T> whereStartsWith(String key, String prefix) {
        this.conditions.whereStartsWith(key, prefix);
        return this;
    }

    public static <T extends JBObject> JBQuery<T> or(List<JBQuery<T>> queries) {
        String className = null;
        if (queries.size() > 0) {
            className = ((JBQuery) queries.get(0)).getClassName();
        }

        JBQuery result = JBQuery.getInstance(className);
        if (queries.size() > 1) {

            for (Object query1 : queries) {
                JBQuery query = (JBQuery) query1;
                if (!className.equals(query.getClassName())) {
                    throw new IllegalArgumentException("All queries must be for the same class");
                }

                result.addOrItems(new QueryOperation("$or", "$or", query.conditions.compileWhereOperationMap()));
            }
        } else {
            result.setWhere(((JBQuery) queries.get(0)).conditions.getWhere());
        }

        return result;
    }

    public JBQuery<T> whereMatchesKeyInQuery(String key, String searchKey, String targetClass, JBQuery<?> query) {
        HashMap<String , Object> inner = new HashMap<>();
        inner.put("searchClass", query.getClassName());
        inner.put("where", query.conditions.compileWhereOperationMap());
        if (query.conditions.getSkip() > 0) {
            inner.put("skip", query.conditions.getSkip());
        }

        if (query.conditions.getLimit() > 0) {
            inner.put("limit", query.conditions.getLimit());
        }

        if (!Utils.isBlankContent(query.getOrder())) {
            inner.put("order", query.getOrder());
        }
        inner.put("searchKey", searchKey);
        inner.put("targetClass", targetClass);
        return this.addWhereItem(key, "$sub", inner);
    }

    public JBQuery<T> whereMatchesQuery(String key, JBQuery<?> query) {
        Map map = Utils.createMap("where", query.conditions.compileWhereOperationMap());
        map.put("className", query.className);
        if (query.conditions.getSkip() > 0) {
            map.put("skip", Integer.valueOf(query.conditions.getSkip()));
        }

        if (query.conditions.getLimit() > 0) {
            map.put("limit", Integer.valueOf(query.conditions.getLimit()));
        }

        if (!Utils.isBlankContent(query.getOrder())) {
            map.put("order", query.getOrder());
        }

        this.addWhereItem(key, "$inQuery", map);
        return this;
    }

    public JBQuery<T> whereDoesNotMatchKeyInQuery(String key, String keyInQuery, JBQuery<?> query) {
        Map map = Utils.createMap("className", query.className);
        map.put("where", query.conditions.compileWhereOperationMap());
        Map queryMap = Utils.createMap("query", map);
        queryMap.put("key", keyInQuery);
        this.addWhereItem(key, "$dontSelect", queryMap);
        return this;
    }

    public JBQuery<T> whereDoesNotMatchQuery(String key, JBQuery<?> query) {
        Map map = Utils.createMap("className", query.className);
        map.put("where", query.conditions.compileWhereOperationMap());
        this.addWhereItem(key, "$notInQuery", map);
        return this;
    }

    JBQuery<T> setWhere(Map<String, List<QueryOperation>> value) {
        this.conditions.setWhere(value);
        return this;
    }

    public void deleteAllInBackground(DeleteCallback deleteCallback){
        assembleParameters();
        manager.deleteByQuery(getParameters() , false , deleteCallback);
    }

    public void deleteAll() throws JBException{
        assembleParameters();
        final Object[] objects = new Object[1];
        manager.deleteByQuery(getParameters(), true, new DeleteCallback() {
            @Override
            public void done() {

            }

            @Override
            public void error(JBException e) {
                objects[0] = e;
            }
        });
        if (objects[0] != null)
            throw (JBException) objects[0];
    }

    public List<JBObject> find() throws JBException{
        assembleParameters();
        final Object[] objects = new Object[2];
        manager.objectQuery(getParameters(), true, new FindCallback<JBObject>() {
            @Override
            public void done(List<JBObject> r) {
                objects[0] = r;
            }

            @Override
            public void error(JBException e) {
                objects[1] = e;
            }
        }, cachePolicy);
        if (objects[1] != null)
            throw ((JBException) objects[1]);
        return (List<JBObject>) objects[0];
    }

    public void findInBackground(FindCallback<JBObject> callback) {
        assembleParameters();
        manager.objectQuery(getParameters(), false ,callback, cachePolicy);
    }

    public int count() throws JBException {
        assembleParameters();
        final Object[] objects = new Object[2];
        manager.countQuery(getParameters(), true , new CountCallback() {
            @Override
            public void done(int count) {
                objects[0] = count;
            }

            @Override
            public void error(JBException e) {
                objects[1] = e;
            }
        });
        if (objects[1] != null)
            throw ((JBException) objects[1]);
        return (int) objects[0];
    }

    public void countInBackground(CountCallback callback) {
        assembleParameters();
        manager.countQuery(getParameters(), false , callback);
    }

    protected Map<String, String> assembleParameters() {
        return this.conditions.assembleParameters();
    }

    public enum CachePolicy {
        CACHE_ELSE_NETWORK,
        CACHE_ONLY,
        CACHE_THEN_NETWORK,
        IGNORE_CACHE,
        NETWORK_ELSE_CACHE,
        NETWORK_ONLY;

        private CachePolicy() {
        }
    }
}
