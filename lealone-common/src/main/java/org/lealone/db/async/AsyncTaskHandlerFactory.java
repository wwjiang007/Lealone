/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package org.lealone.db.async;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncTaskHandlerFactory {

    private static AsyncTaskHandler DEFAULT_HANDLER = new AsyncTaskHandler() {
        @Override
        public void handle(AsyncTask task) {
            task.run();
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(AsyncTask task, long initialDelay, long delay,
                TimeUnit unit) {
            task.run();
            return null;
        }
    };

    private static final AtomicInteger index = new AtomicInteger(0);
    private static AsyncTaskHandler[] handlers = { DEFAULT_HANDLER };

    public static void setAsyncTaskHandlers(AsyncTaskHandler[] handlers) {
        AsyncTaskHandlerFactory.handlers = handlers;
    }

    public static AsyncTaskHandler getAsyncTaskHandler() {
        return handlers[index.getAndIncrement() % handlers.length];
    }
}
