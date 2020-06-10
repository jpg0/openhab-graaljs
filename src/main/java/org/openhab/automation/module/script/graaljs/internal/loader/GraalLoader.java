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

package org.openhab.automation.module.script.graaljs.internal.loader;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.graalvm.polyglot.Context;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.InstructionAdapter;
import org.openhab.automation.module.script.graaljs.internal.GraalJSScriptEngineFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Class to load Graal. This smoothes out the clashes between Graal and OSGi. Call {@link GraalLoader#loadGraal} to
 * load Graal into the current bundle before using it's classes.
 *
 * Currently:
 * - Denies access to Nashorn so that it doesn't overwrite all it's properties.
 * - Loads various classes in the correct classloader so that they don't leak into parent classloaders
 * - Alters to JavaAdapterServices to lookup method handles from itself, rather than Object.class; this prevents it's
 * lookups leaking classes into the bootstrap classloader.
 *
 * Note that this class doesn't directly implement {@link WeavingHook}, because if it did so then it would attempt to weave
 * itself and create a circular dependency.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class GraalLoader {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private BundleContext bundleContext;

    public GraalLoader(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void loadGraal(){
        bundleContext.registerService(WeavingHook.class, this::weave, null);

        /*
        Graal will attempt to neuter Nashorn by making it support zero (well, null) languages. This will cause problems
        for any code that attempts to use (or is using) Nashorn in another classloader (e.g. another bundle), as Graal
        will not be available in those classloaders (and this would result in Nashorn not being either). Prevent this
        by preventing Nashorn being seen in Graal's startup
         */

        LoaderUtils.withContextClassloader(() -> {
            try {
                Class.forName("com.oracle.truffle.js.scriptengine.GraalJSEngineFactory");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            /* Graal will also try to load bits and pieces from the wrong classloader at <clinit> time from it's Engine. This
            is only a problem for reloading the bundle as some classes are leaked. This attempts to contain them in the
            current bundle's classloader */
//            Engine.newBuilder().build();

            /* Actually create the first engine instance in the right classloader */
            GraalJSScriptEngine.create(null,
                    Context.newBuilder("js")
                            .allowExperimentalOptions(true)
                            .allowAllAccess(true)
                            .option("js.nashorn-compat", "true")
                            .option("js.commonjs-require", "true")).put("test", "object");

        }, cl -> new ClassLoader(GraalJSScriptEngineFactory.class.getClassLoader()) {
            @Override
            @NonNullByDefault({})
            public Enumeration<URL> getResources(String name) throws IOException {
                if ("META-INF/services/javax.script.ScriptEngineFactory".equals(name)) {
                    return Collections.emptyEnumeration();
                }
                return super.getResources(name);
            }
        });
    }

    public void weave(WovenClass wovenClass) {
        if(wovenClass.getClassName().equals("com.oracle.truffle.js.runtime.java.adapter.JavaAdapterServices")) {
            log.info("Instrumenting {}", wovenClass.getClassName());
            final ClassReader cr = new ClassReader(wovenClass.getBytes());
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            final ClassVisitor v = new RebindMethodAdapter(cw, "<clinit>", "publicLookup", "lookup");
            cr.accept(v, 0);
            wovenClass.setBytes(cw.toByteArray());
        }
    }

    static class RebindMethodAdapter extends ClassVisitor {
        private String inMethodName, oldCall, newCall;

        public RebindMethodAdapter(ClassWriter cw, String inMethodName, String oldCall, String newCall) {
            super(Opcodes.ASM6, cw);
            this.inMethodName = inMethodName;
            this.oldCall = oldCall;
            this.newCall = newCall;
        }

        @NonNullByDefault({})
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            if (name.equals(inMethodName)) {
                return replaceCall(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private MethodVisitor replaceCall(MethodVisitor visitor, final int access, final String name, final String desc){
            return new InstructionAdapter(Opcodes.ASM6, visitor) {
                @Override
                @NonNullByDefault({})
                public void invokestatic(final String owner, String name, final String descriptor, final boolean isInterface) {
                    if(name.equals(oldCall)) {
                        name = newCall;
                    }

                    super.invokestatic(owner, name, descriptor, isInterface);
                }
            };
        };
    }
}
