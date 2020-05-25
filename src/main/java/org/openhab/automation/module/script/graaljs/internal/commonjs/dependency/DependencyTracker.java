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

package org.openhab.automation.module.script.graaljs.internal.commonjs.dependency;

import org.openhab.core.automation.module.script.ScriptEngineContainer;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Tracks dependencies between scripts and reloads dependers
 *
 * @author Jonathan Gilbert
 */
@Component(service = DependencyTracker.class)
public class DependencyTracker {

    private Logger logger = LoggerFactory.getLogger(DependencyTracker.class);

    private ScriptEngineManager manager = null;
    private final BidiSetBag<String, String> scriptToLibs = new BidiSetBag<>();
    private ScriptLibraryListener scriptLibraryListener = new ScriptLibraryListener() {
        @Override
        void updateFile(String libraryPath) {
            Set<String> scripts;
            synchronized (scriptToLibs) {
                scripts = new HashSet<>(scriptToLibs.getKeys(libraryPath)); //take a copy as it will change as we reimport
            }
            DependencyTracker.this.logger.debug("Library {} changed; reimporting {} scripts...", libraryPath, scripts.size());
            for(String scriptUrl : scripts) {
                reimportScript(scriptUrl);
            }
        }
    };
    private BundleContext bundleContext;

    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private ScriptEngineManager getManager() {
        if(manager == null) {
            manager = bundleContext.getService(bundleContext.getServiceReference(ScriptEngineManager.class));
        }
        return manager;
    }

    @Reference
    public void setScriptEngineManager(ScriptEngineManager manager) {
        this.manager = manager;
    }

    public void reimportScript(String scriptPath) {
        logger.debug("Reimporting {}...", scriptPath);
        manager.removeEngine(scriptPath);

        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(new URL(scriptPath).openStream()))) {
            logger.info("Loading script '{}'", scriptPath);

            ScriptEngineContainer container = manager.createScriptEngine("js", scriptPath);

            if (container != null) {
                getManager().loadScript(container.getIdentifier(), reader);
                logger.debug("Script loaded: {}", scriptPath);
            } else {
                logger.error("Script loading error, ignoring file: {}", scriptPath);
            }
        } catch (IOException e) {
            logger.error("Failed to load file '{}': {}", scriptPath, e.getMessage());
        }
    }

    public void addLibForScript(String scriptPath, String libPath) {
        synchronized (scriptToLibs) {
            scriptToLibs.put(scriptPath, libPath);
        }
    }

    public void removeScript(String scriptPath) {
        synchronized (scriptToLibs) {
            scriptToLibs.removeKey(scriptPath);
        }
    }
}