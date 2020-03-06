package org.openhab.automation.module.script.extension.item;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.openhab.automation.module.script.extension.provider.FixedProvider;

import java.util.Collection;

@NonNullByDefault
public class ScriptItemProvider extends FixedProvider<Item> implements ItemProvider {
    public ScriptItemProvider(Collection<Item> all) {
        super(all);
    }
}
