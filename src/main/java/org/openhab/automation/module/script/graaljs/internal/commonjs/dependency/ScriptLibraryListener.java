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

import org.eclipse.smarthome.core.service.AbstractWatchService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.openhab.automation.module.script.graaljs.internal.commonjs.CommonJSScriptEngine.LIB_PATH;

/**
 * Listens for changes to script libraries
 *
 * @author Jonathan Gilbert
 */
abstract class ScriptLibraryListener extends AbstractWatchService {

    ScriptLibraryListener() {
        super(LIB_PATH);
    }

    @Override
    protected boolean watchSubDirectories() {
        return true;
    }

    @Override
    protected WatchEvent.Kind<?>[] getWatchEventKinds(Path path) {
        return new WatchEvent.Kind<?>[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
    }

    @Override
    protected void processWatchEvent(WatchEvent<?> watchEvent, WatchEvent.Kind<?> kind, Path path) {
        File file = path.toFile();
        if (!file.isHidden()) {
            if (kind.equals(ENTRY_DELETE)) {
                this.updateFile(file.getPath());
            }

            if (file.canRead() && (kind.equals(ENTRY_CREATE) || kind.equals(ENTRY_MODIFY))) {
                this.updateFile(file.getPath());
            }
        }
    }

    abstract void updateFile(String filePath);
}