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
package org.openhab.automation.module.script.graaljs.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.module.script.graaljs.internal.commonjs.graaljs.GraalJSCommonJSScriptEngineManager;
import org.openhab.automation.module.script.graaljs.internal.loader.GraalLoader;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.automation.module.script.graaljs.internal.commonjs.ScriptExtensionModuleProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.script.ScriptEngine;
import java.util.*;

/**
 * An implementation of {@link ScriptEngineFactory} with customizations for GraalJS ScriptEngines.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
@Component(service = ScriptEngineFactory.class)
public final class GraalJSScriptEngineFactory implements ScriptEngineFactory {

    @NonNullByDefault({})
    private ScriptExtensionModuleProvider scriptExtensionModuleProvider;
    @NonNullByDefault({})
    private GraalJSCommonJSScriptEngineManager commonJSScriptEngineManager;


    /*
    we could get these from GraalJSEngineFactory, but this causes problems with OSGi. This class attempts to replace
    (well, fill with nulls) Nashorn at class load time (which is generally ok), but if loaded a second time (e.g. the
    bundle is reloaded) then it ironically chokes at <clinit> time due to the state that it's left Nashorn in.
     */
    private static List<String> mimeTypes = Arrays.asList("application/javascript", "application/ecmascript", "text/javascript", "text/ecmascript");
    private static List<String> extensions = Collections.singletonList("js");

    private static final String DISABLE_GRAALJS_SCRIPT_DEBUG = "graaljs.script.debug.disabled";

    @Activate
    public void activate(BundleContext bundleContext) {
        // Load Graal at activation time
//        new GraalLoader(bundleContext).loadGraal();
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setScriptExtensionModuleProvider(ScriptExtensionModuleProvider scriptExtensionModuleProvider) {
        this.scriptExtensionModuleProvider = scriptExtensionModuleProvider;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setCommonJSScriptEngineManager(GraalJSCommonJSScriptEngineManager commonJSScriptEngineManager) {
        this.commonJSScriptEngineManager = commonJSScriptEngineManager;
    }

    @Override
    public List<String> getScriptTypes() {
        List<String> scriptTypes = new ArrayList<>();

        scriptTypes.addAll(mimeTypes);
        scriptTypes.addAll(extensions);

        return Collections.unmodifiableList(scriptTypes);
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        //noop; the are retrieved via modules, not injected
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        ScriptEngine engine = commonJSScriptEngineManager.create(scriptExtensionModuleProvider);
        configureEngine(engine);
        return engine;
    }

    private ScriptEngine configureEngine(ScriptEngine engine) {

        // log stack traces in user code if requested
        if (!Boolean.getBoolean(DISABLE_GRAALJS_SCRIPT_DEBUG)) {
            engine = DebuggingGraalScriptEngine.create(engine);
        }

        return engine;
    }
}
