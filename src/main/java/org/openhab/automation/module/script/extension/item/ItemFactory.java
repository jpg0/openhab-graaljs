package org.openhab.automation.module.script.extension.item;

import org.openhab.core.items.Item;
import org.openhab.automation.module.script.extension.provider.LifecycleAwareDelegate;
import org.openhab.automation.module.script.extension.rule.ScriptRuleProvider;
import org.openhab.automation.module.script.graaljs.internal.commonjs.LifecycleAwareSet;
import org.openhab.core.automation.Rule;

import java.util.ArrayList;
import java.util.Arrays;

public class ItemFactory extends LifecycleAwareSet {
    public ScriptItemProvider newItemProvider(Item[] items) {
        return new ScriptItemProvider(new ArrayList<Item>(Arrays.asList(items)));
    }

    public ScriptItemProvider registeringRuleProvider(Item[] items) {
        ScriptItemProvider provider = newItemProvider(items);
        addLifecycleAware(new LifecycleAwareDelegate<>(provider, ScriptItemProvider.class));
        return provider;
    }
}
