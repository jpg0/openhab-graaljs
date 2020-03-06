package org.openhab.automation.module.script.extension.item;

import org.eclipse.smarthome.core.items.Item;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRule;

import java.util.Map;
import java.util.function.BiFunction;

public class JSItem {

    private Context ctx;
    private Value properties;

    public JSItem(Context ctx) {
        this.ctx = ctx;
        this.properties = ctx.eval("js", "{}");;
    }

    public Value getProperties() {
        return properties;
    }

}
