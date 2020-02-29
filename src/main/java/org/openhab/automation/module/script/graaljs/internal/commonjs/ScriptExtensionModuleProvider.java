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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.automation.module.script.rulesupport.shared.ScriptedAutomationManager;
import org.graalvm.polyglot.Context;
import org.openhab.automation.module.script.graaljs.commonjs.internal.MapModule;
import org.openhab.automation.module.script.graaljs.commonjs.internal.ModuleLocator;
import org.openhab.automation.module.script.graaljs.internal.threading.ThreadsafeWrappingScriptedAutomationManagerDelegate;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.script.ScriptEngine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class providing script extensions via CommonJS modules.
 *
 * @author Jonathan Gilbert - Initial contribution
 */

@NonNullByDefault
@Component(service = ScriptExtensionModuleProvider.class)
public class ScriptExtensionModuleProvider {

    private static final String RUNTIME_MODULE_PREFIX = "@runtime";
    private static final String DEFAULT_MODULE_NAME = "Defaults";

    private @NonNullByDefault({}) ScriptExtensionManager scriptExtensionManager;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setScriptExtensionManager(ScriptExtensionManager scriptExtensionManager) {
        this.scriptExtensionManager = scriptExtensionManager;
    }

    ModuleLocator locatorFor(Context ctx, String engineIdentifier) {
        return name -> {
            String[] segments = name.split("/");
            if(segments[0].equals(RUNTIME_MODULE_PREFIX)){
                if(segments.length == 1) {
                    return runtimeModule(DEFAULT_MODULE_NAME, engineIdentifier, ctx);
                } else {
                    return runtimeModule(segments[1], engineIdentifier, ctx);
                }
            }

            return Optional.empty();
        };
    }

    private Optional<MapModule> runtimeModule(String name, String scriptIdentifier, Context ctx) {
        ValueCapturingScriptEngineFactory scopeHolder = new ValueCapturingScriptEngineFactory();

        if(DEFAULT_MODULE_NAME.equals(name)) {
            scriptExtensionManager.importDefaultPresets(scopeHolder, null, scriptIdentifier);
        } else {
            scriptExtensionManager.importPreset(name, scopeHolder, null, scriptIdentifier);
        }

        return Optional.ofNullable(scopeHolder.scopeValues)
                .map(this::processValues)
                .map(v -> new MapModule(v, ctx));
    }

    private Map<String, Object> processValues(Map<String, Object> values) {
        Map<String, Object> rv = new HashMap<>(values);

        for(Map.Entry<String, Object> entry : rv.entrySet()) {
            if(entry.getValue() instanceof ScriptedAutomationManager) {
                entry.setValue(new ThreadsafeWrappingScriptedAutomationManagerDelegate((ScriptedAutomationManager)entry.getValue()));
            }
        }

        return rv;
    }

    private static class ValueCapturingScriptEngineFactory implements ScriptEngineFactory {

        @NonNullByDefault({}) //set later via the scopeValues call
        Map<String, Object> scopeValues = new HashMap<>();

        @Override
        public List<String> getScriptTypes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
            this.scopeValues.putAll(scopeValues);
        }

        @Override
        public @Nullable ScriptEngine createScriptEngine(String scriptType) {
            throw new UnsupportedOperationException();
        }
    }
}
