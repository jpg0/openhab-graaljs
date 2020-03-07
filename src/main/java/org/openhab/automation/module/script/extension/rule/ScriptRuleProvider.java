package org.openhab.automation.module.script.extension.rule;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.module.script.extension.provider.SuspendableFixedProvider;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleProvider;

import java.util.*;

@NonNullByDefault
public class ScriptRuleProvider extends SuspendableFixedProvider<Rule> implements RuleProvider {
    public ScriptRuleProvider(Collection<Rule> all) {
        super(all);
    }
}
