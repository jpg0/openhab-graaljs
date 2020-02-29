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
import org.graalvm.polyglot.Value;

/**
 * Interface providing access to a CommonJS module.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public interface ScriptModule {
    /**
     * The symbols/properties exported by this module
     * @return the exported properties
     */
    Value getExports();

    /**
     * The properties/metadata related to this module
     * @return the module properties
     */
    Value getModule();
}
