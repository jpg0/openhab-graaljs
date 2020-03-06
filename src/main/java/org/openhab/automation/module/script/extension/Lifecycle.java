/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.module.script.extension;

import org.openhab.automation.module.script.graaljs.internal.commonjs.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * Allows scripts to register for lifecycle events
 *
 * @author Jonathan Gilbert
 */
public class Lifecycle implements LifecycleAware {
    private static final Logger logger = LoggerFactory.getLogger(Lifecycle.class);
    public static final int DEFAULT_PRIORITY = 50;
    private Map<Event, List<Hook>> listenersByType = new HashMap<>();

    public void addDisposeHook(Consumer<Object> listener, int priority) {
        addListenerOfType(Event.DISPOSED, listener, priority);
    }

    public void addDisposeHook(Consumer<Object> listener) {
        addDisposeHook(listener, DEFAULT_PRIORITY);
    }

    public void addLoadedHook(Consumer<Object> listener, int priority) {
        addListenerOfType(Event.LOADED, listener, priority);
    }

    public void addLoadedHook(Consumer<Object> listener) {
        addLoadedHook(listener, DEFAULT_PRIORITY);
    }

    private void addListenerOfType(Event e, Consumer<Object> listener, int priority) {
        listenersByType.putIfAbsent(e, new ArrayList<>());
        listenersByType.get(e).add(new Hook(priority, listener));
    }

    @Override
    public void onLifecycleEvent(Event e, String scriptIdentifier) {
        try {
            listenersByType.getOrDefault(e, new ArrayList<>()).stream().sorted(Comparator.comparingInt(h -> h.priority))
                    .forEach(h -> h.fn.accept(scriptIdentifier));
        } catch (RuntimeException ex) {
            logger.warn("Lifecycle processing of event {} halted due to exception to dispose: {}: {}", e, ex.getClass(),
                    ex.getMessage());
        }
    }

    private static class Hook {
        public Hook(int priority, Consumer<Object> fn) {
            this.priority = priority;
            this.fn = fn;
        }

        int priority;
        Consumer<Object> fn;
    }
}

