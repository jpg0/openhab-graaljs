package org.openhab.automation.module.script.extension.rule;

import org.openhab.automation.module.script.extension.AbstractScriptExtensionProvider;
import org.openhab.automation.module.script.extension.module.GraalJSPrivateModuleHandlerFactory;
import org.openhab.core.automation.module.script.ScriptExtensionProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY;

@Component(immediate = true, service = ScriptExtensionProvider.class)
public class RuleExtensionProvider extends AbstractScriptExtensionProvider {

    @Reference(cardinality = MANDATORY)
    public void setGraalJSPrivateModuleHandlerFactory(
            GraalJSPrivateModuleHandlerFactory graalJSPrivateModuleHandlerFactory) {
        this.graalJSPrivateModuleHandlerFactory = graalJSPrivateModuleHandlerFactory;
    }

    private GraalJSPrivateModuleHandlerFactory graalJSPrivateModuleHandlerFactory;

    @Override
    protected String getPresetName() {
        return "rules";
    }

    @Override
    protected void initializeTypes(BundleContext context) {
        addType("factory", k -> new RuleFactory(graalJSPrivateModuleHandlerFactory));
    }
}
