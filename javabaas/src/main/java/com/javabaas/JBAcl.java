package com.javabaas;

/**
 * Created by xueshukai on 15/9/28 上午11:13.
 */

import java.util.HashMap;
import java.util.Map;

public class JBAcl {
    private final Map<String, Object> permissionsById = new HashMap();
    private static String readTag = "read";
    private static String writeTag = "write";
    private static String publicTag = "*";

    public JBAcl() {
    }

    public JBAcl(Map<String,Object> acl){
        permissionsById.putAll(acl);
    }

    JBAcl(JBAcl right) {
        this.permissionsById.putAll(right.permissionsById);
    }

    public JBAcl(JBUser owner) {
        this.setReadAccess(owner, true);
        this.setWriteAccess(owner, true);
    }

    private Map<String, Object> mapForKey(String key, boolean create) {
        Object map = (Map) this.permissionsById.get(key);
        if (map == null && create) {
            map = new HashMap();
            this.permissionsById.put(key, map);
        }

        return (Map) map;
    }

    private void allowRead(boolean allowed, String key) {
        Map map = this.mapForKey(key, allowed);
        if (allowed) {
            map.put(readTag, Boolean.valueOf(true));
        } else if (map != null) {
            map.remove(readTag);
        }

    }

    private boolean isReadAllowed(String key) {
        Map map = this.mapForKey(key, false);
        return map != null && (Boolean) map.get(readTag) != null && ((Boolean) map.get(readTag)).booleanValue();
    }

    private void allowWrite(boolean allowed, String key) {
        Map map = this.mapForKey(key, allowed);
        if (allowed) {
            map.put(writeTag, Boolean.valueOf(allowed));
        } else if (map != null) {
            map.remove(writeTag);
        }

    }

    private boolean isWriteAllowed(String key) {
        Map map = this.mapForKey(key, false);
        return map != null && (Boolean) map.get(writeTag) != null && ((Boolean) map.get(writeTag)).booleanValue();
    }

    public boolean getPublicReadAccess() {
        return this.isReadAllowed(publicTag);
    }

    public boolean getPublicWriteAccess() {
        return this.isWriteAllowed(publicTag);
    }

    public boolean getReadAccess(JBUser user) {
        return this.getReadAccess(user.getId());
    }

    public boolean getReadAccess(String userId) {
        return this.isReadAllowed(userId);
    }

    public boolean getWriteAccess(JBUser user) {
        return this.getWriteAccess(user.getId());
    }

    public boolean getWriteAccess(String userId) {
        return this.isWriteAllowed(userId);
    }

    public static JBAcl parseACLWithPublicAccess(boolean read, boolean write) {
        JBAcl acl = new JBAcl();
        acl.setPublicReadAccess(read);
        acl.setPublicWriteAccess(write);
        return acl;
    }

    public void setPublicReadAccess(boolean allowed) {
        this.allowRead(allowed, publicTag);
    }

    public void setPublicWriteAccess(boolean allowed) {
        this.allowWrite(allowed, publicTag);
    }

    public void setReadAccess(JBUser user, boolean allowed) {
        this.setReadAccess(user.getId(), allowed);
    }

    public void setReadAccess(String userId, boolean allowed) {
        this.allowRead(allowed, userId);
    }

    public void setWriteAccess(JBUser user, boolean allowed) {
        this.setWriteAccess(user.getId(), allowed);
    }

    public void setWriteAccess(String userId, boolean allowed) {
        this.allowWrite(allowed, userId);
    }

    public Map<String, Object> getPermissionsById() {
        return this.permissionsById;
    }

    public Map<String, Object> getACLMap() {
        HashMap aclMap = new HashMap();
        aclMap.put("acl", this.getPermissionsById());
        return aclMap;
    }

}
