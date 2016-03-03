package com.javabaas.callback;



import java.util.List;

import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 15/9/29 下午2:53.
 */
public interface FindCallback<T> {
    void done(List<T> result);
    void error(JBException e);
}
