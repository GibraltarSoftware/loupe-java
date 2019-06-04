package com.onloupe.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * A configurable copy of the DefaultThreadFactory, because Java could not possibly
 * allow us to configure the native one.
 * 
 * @author RyanKelliher
 *
 */
public class GibraltarThreadFactory implements ThreadFactory {
    
    /** The Constant poolNumber. */
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    
    /** The group. */
    private final ThreadGroup group;
    
    /** The thread number. */
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    
    /** The name prefix. */
    private final String namePrefix;

    /**
     * Instantiates a new gibraltar thread factory.
     *
     * @param ourPrefix the our prefix
     */
    public GibraltarThreadFactory(String ourPrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                              Thread.currentThread().getThreadGroup();
        namePrefix = ourPrefix + "-pool-" +
                      poolNumber.getAndIncrement() +
                     "-thread-";
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
