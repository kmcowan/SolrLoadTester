/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solrloadtester;

import java.util.LinkedHashMap;

/**
 *
 * @author kevin
 */
public class ThreadQueue<T,V> extends LinkedHashMap {
    private static ThreadQueue<String,Runnable> queue = null;
    
    private ThreadQueue(){
        
    }
    
    public static ThreadQueue getInstance(){
        if(queue == null){
            queue = new ThreadQueue<>();
        }
        return queue;
    }
}
