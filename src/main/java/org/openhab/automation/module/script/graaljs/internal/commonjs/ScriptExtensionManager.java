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
package org.openhab.automation.module.script.graaljs.internal.commonjs;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.script.ScriptEngine;

import org.openhab.automation.module.script.extension.Lifecycle;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptExtensionProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Direct copy of {@link org.openhab.core.automation.module.script.internal.ScriptExtensionManager},
 * which we cannot use because it's not exported :(
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@Component(service = ScriptExtensionManager.class)
public class ScriptExtensionManager {
    private Set<ScriptExtensionProvider> scriptExtensionProviders = new CopyOnWriteArraySet<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addScriptExtensionProvider(ScriptExtensionProvider provider) {
        scriptExtensionProviders.add(provider);
    }

    public void removeScriptExtensionProvider(ScriptExtensionProvider provider) {
        scriptExtensionProviders.remove(provider);
    }

    public void addExtension(ScriptExtensionProvider provider) {
        scriptExtensionProviders.add(provider);
    }

    public void removeExtension(ScriptExtensionProvider provider) {
        scriptExtensionProviders.remove(provider);
    }

    public Object get(String type, String scriptIdentifier) {
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            if (provider.getTypes().contains(type)) {
                return provider.get(scriptIdentifier, type);
            }
        }

        return null;
    }

    public List<String> getDefaultPresets() {
        List<String> defaultPresets = new ArrayList<>();

        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            defaultPresets.addAll(provider.getDefaultPresets());
        }

        return defaultPresets;
    }

    public void importDefaultPresets(ScriptEngineFactory engineProvider, ScriptEngine scriptEngine,
            String scriptIdentifier) {
        for (String preset : getDefaultPresets()) {
            importPreset(preset, engineProvider, scriptEngine, scriptIdentifier);
        }
    }

    public Map<String, Object> importPreset(String preset, ScriptEngineFactory engineProvider, ScriptEngine scriptEngine,
            String scriptIdentifier) {
        Map<String, Object> allValues = new HashMap<>();
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            if (provider.getPresets().contains(preset)) {
                Map<String, Object> scopeValues = provider.importPreset(scriptIdentifier, preset);

                engineProvider.scopeValues(scriptEngine, scopeValues);
                allValues.putAll(scopeValues);
            }
        }
        return allValues;
    }

    void notifyScriptLoaded(String scriptIdentifier) {
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            if (provider instanceof LifecycleAware) {
                ((LifecycleAware) provider).onLifecycleEvent(LifecycleAware.Event.LOADED, scriptIdentifier);
            }
        }
    }
}