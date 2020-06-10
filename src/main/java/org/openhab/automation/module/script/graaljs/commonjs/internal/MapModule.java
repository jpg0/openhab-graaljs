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
package org.openhab.automation.module.script.graaljs.commonjs.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.util.Map;

/**
 * {@link ScriptModule} implementation providing access to a set of POJOs in a Map.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class MapModule implements ScriptModule {

    private Value exports;
    private Value module;

    public MapModule(Map<String, Object> values, Context ctx) {
//        try {
//            exports = ctx.eval(Source.newBuilder( //convert to Map to JS Object
//                    "js",
//                    "(function (mapOfValues) {\n" +
//                            "let rv = {};\n" +
//                            "for (var key in mapOfValues) {\n" +
//                            "    rv[key] = mapOfValues.get(key);\n" +
//                            "}\n" +
//                            "return rv;\n" +
//                            "})",
//                    "<generated>"
//            ).build()).execute(values);
            exports = ctx.asValue(values);
//        } catch (IOException e) {
//            throw new IllegalStateException("Failed to generate exports");
//        }

        module = ctx.eval("js", "({})");
        assert module != null;
        assert exports != null;
    }

    @Override
    public Value getExports() {
        return exports;
    }

    @Override
    public Value getModule() {
        return module;
    }
}
