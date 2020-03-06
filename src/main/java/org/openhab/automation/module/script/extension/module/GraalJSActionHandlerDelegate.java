package org.openhab.automation.module.script.extension.module;

import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.BaseActionModuleHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraalJSActionHandlerDelegate extends BaseActionModuleHandler {

    private org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleActionHandler actionHandler;

    public GraalJSActionHandlerDelegate(Action module,
            org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleActionHandler actionHandler) {
        super(module);
        this.actionHandler = actionHandler;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> inputs) {
        Set<String> keys = new HashSet<>(inputs.keySet());

        Map<String, Object> extendedInputs = new HashMap<>(inputs);
        for (String key : keys) {
            Object value = extendedInputs.get(key);
            int dotIndex = key.indexOf('.');
            if (dotIndex != -1) {
                String moduleName = key.substring(0, dotIndex);
                extendedInputs.put("module", moduleName);
                String newKey = key.substring(dotIndex + 1);
                extendedInputs.put(newKey, value);
            }
        }

        Object result = actionHandler.execute(module, extendedInputs);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", result);
        return resultMap;
    }
}
