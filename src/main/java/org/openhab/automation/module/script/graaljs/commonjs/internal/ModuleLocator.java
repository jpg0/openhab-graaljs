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

import java.util.Optional;

/**
 * Interface describing single method to locate a CommonJS module.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public interface ModuleLocator {
    Optional<? extends ScriptModule> locateModule(String name);
}
