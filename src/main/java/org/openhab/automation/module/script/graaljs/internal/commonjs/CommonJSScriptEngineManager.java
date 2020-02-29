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

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.openhab.automation.module.script.graaljs.internal.commonjs.dependency.DependencyTracker;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.script.ScriptEngine;

/**
 * @author Jonathan Gilbert
 */
@Component(service = CommonJSScriptEngineManager.class)
public class CommonJSScriptEngineManager {
    private DependencyTracker dependencyTracker;

    @Reference
    public void setDependencyTracker(DependencyTracker dependencyTracker) {
        this.dependencyTracker = dependencyTracker;
    }

    public ScriptEngine create(GraalJSScriptEngine engine, ScriptExtensionModuleProvider scriptExtensionModuleProvider) {
        CommonJSScriptEngine commonJSScriptEngine = CommonJSScriptEngine.create(engine, scriptExtensionModuleProvider, dependencyTracker);
        return commonJSScriptEngine.createProxy();
    }
}
