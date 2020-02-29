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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows scripts to register for lifecycle events
 *
 * @author Jonathan Gilbert
 */
public class Lifecycle implements Disposable {
   private static final Logger logger = LoggerFactory.getLogger(Lifecycle.class);
   private List<Disposable> disposables = new ArrayList<>();

   public void addDisposeHook(Disposable disposable) {
       disposables.add(disposable);
   }

   public void dispose() {
       for(Disposable disposable : disposables) {
           try {
               disposable.dispose();
           } catch(Exception e) {
               logger.warn("Failed to dispose: {}: {}", e.getClass(), e.getMessage());
           }
       }
   }
}