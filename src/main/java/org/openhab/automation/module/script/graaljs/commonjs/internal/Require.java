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
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

/**
 * Class to register commonjs support / 'require' property to a specific context
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class Require {

  public static JSModule enable(Context ctx, Folder folder, Iterable<Folder> libPaths) throws PolyglotException {
    return enable(ctx, folder, ctx.getBindings("js"), libPaths, null);
  }

  // This overload registers the require function in a specific Binding. It is useful when re-using the
  // same script engine across multiple threads (each thread should have his own global scope defined
  // through the binding that is passed as an argument).
  public static JSModule enable(Context ctx, Folder folder, Value bindings, Iterable<Folder> libPaths, @Nullable ModuleLocator moduleLocator)
      throws PolyglotException {
    Value module = ctx.eval("js", "({})");
    Value exports = ctx.eval("js", "({})");

    JSModule created =
        new JSModule(ctx, folder, libPaths, new ModuleCache(),"<main>", module, exports, null, null, moduleLocator);
    created.setLoaded();

    bindings.putMember("require", created);
    bindings.putMember("module", module);
    bindings.putMember("exports", exports);

    return created;
  }
}
