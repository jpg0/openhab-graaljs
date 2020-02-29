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
package org.openhab.automation.module.script.extension;

import org.openhab.automation.module.script.extension.binding.BindingItemProviderDelegateFactory;
import org.openhab.core.automation.module.script.ScriptExtensionProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import static org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY;

/**
 * ScriptExtensionProvider which provides support for object providers
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@Component(immediate = true, service = ScriptExtensionProvider.class)
public class ProvidersScriptExtensionProvider extends AbstractScriptExtensionProvider {

    private BindingItemProviderDelegateFactory bindingItemProviderDelegateFactory;

    @Reference(cardinality = MANDATORY)
    public void setBindingItemProviderDelegateFactory(BindingItemProviderDelegateFactory bindingItemProviderDelegateFactory) {
        this.bindingItemProviderDelegateFactory = bindingItemProviderDelegateFactory;
    }

    @Override
    String getPresetName() {
        return "provider";
    }

    @Override
    void initializeTypes(final BundleContext context) {
        addType("itemBinding", k -> bindingItemProviderDelegateFactory);
    }
}

