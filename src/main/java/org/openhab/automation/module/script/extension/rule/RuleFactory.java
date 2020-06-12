package org.openhab.automation.module.script.extension.rule;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.openhab.automation.module.script.extension.module.GraalJSPrivateModuleHandlerFactory;
import org.openhab.automation.module.script.extension.provider.LifecycleAwareDelegate;
import org.openhab.automation.module.script.graaljs.internal.commonjs.LifecycleAware;
import org.openhab.automation.module.script.graaljs.internal.commonjs.LifecycleAwareSet;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleActionHandler;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRuleActionHandler;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRuleActionHandlerDelegate;
import org.openhab.core.automation.util.ActionBuilder;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.automation.util.RuleBuilder;

import java.util.*;

public class RuleFactory extends LifecycleAwareSet {
    private final Set<String> privateHandlers = new HashSet<>();
    private final GraalJSPrivateModuleHandlerFactory graalJSPrivateModuleHandlerFactory;

    RuleFactory(GraalJSPrivateModuleHandlerFactory graalJSPrivateModuleHandlerFactory) {
        this.graalJSPrivateModuleHandlerFactory = graalJSPrivateModuleHandlerFactory;
    }

    public ScriptRuleProvider newRuleProvider(Rule[] rules) {
        return new ScriptRuleProvider(new ArrayList<Rule>(Arrays.asList(rules)));
    }

    public ScriptRuleProvider registeringRuleProvider(Rule[] rules) {
        ScriptRuleProvider provider = newRuleProvider(rules);
        addLifecycleAware(new LifecycleAwareDelegate<>(provider, ScriptRuleProvider.class));
        return provider;
    }

    public Rule processRule(Rule element) {
        return processRuleInternal(element);
    }

    /// internals

    private void removePrivateHandler(String privId) {
        if (privateHandlers.remove(privId)) {
            graalJSPrivateModuleHandlerFactory.removeHandler(privId);
        }
    }

    public String addPrivateActionHandler(SimpleActionHandler actionHandler) {
        String uid = graalJSPrivateModuleHandlerFactory.addHandler(actionHandler);
        privateHandlers.add(uid);
        return uid;
    }

    private Rule processRuleInternal(Rule element) {
        RuleBuilder builder = RuleBuilder.create(element.getUID());

        String name = element.getName();
        if (name == null || name.isEmpty()) {
            name = element.getClass().getSimpleName();
            if (name.contains("$")) {
                name = name.substring(0, name.indexOf('$'));
            }
        }

        builder.withName(name).withDescription(element.getDescription()).withTags(element.getTags());

        // used for numbering the modules of the rule
        int moduleIndex = 1;

        try {
            List<Condition> conditions = new ArrayList<>();
            for (Condition cond : element.getConditions()) {
                Condition toAdd = cond;
                if (cond.getId().isEmpty()) {
                    toAdd = ModuleBuilder.createCondition().withId(Integer.toString(moduleIndex++))
                            .withTypeUID(cond.getTypeUID()).withConfiguration(cond.getConfiguration())
                            .withInputs(cond.getInputs()).build();
                }

                conditions.add(toAdd);
            }

            builder.withConditions(conditions);
        } catch (Exception ex) {
            // conditions are optional
        }

        try {
            List<Trigger> triggers = new ArrayList<>();
            for (Trigger trigger : element.getTriggers()) {
                Trigger toAdd = trigger;
                if (trigger.getId().isEmpty()) {
                    toAdd = ModuleBuilder.createTrigger().withId(Integer.toString(moduleIndex++))
                            .withTypeUID(trigger.getTypeUID()).withConfiguration(trigger.getConfiguration()).build();
                }

                triggers.add(toAdd);
            }

            builder.withTriggers(triggers);
        } catch (Exception ex) {
            // triggers are optional
        }

        List<Action> actions = new ArrayList<>();
        actions.addAll(element.getActions());

        if (element instanceof SimpleRuleActionHandler) {
            String privId = addPrivateActionHandler(
                    new SimpleRuleActionHandlerDelegate((SimpleRuleActionHandler) element));

            Action scriptedAction = ActionBuilder.create().withId(Integer.toString(moduleIndex++))
                    .withTypeUID("graaljs.ScriptedAction").withConfiguration(new Configuration()).build();
            scriptedAction.getConfiguration().put("privId", privId);
            actions.add(scriptedAction);
        }

        builder.withActions(actions);

        Rule rule = builder.build();

        return rule;
    }

    @Override
    public void onLifecycleEvent(Event e, String scriptIdentifier) {
        super.onLifecycleEvent(e, scriptIdentifier);

        if(e.equals(Event.DISPOSED)) {
            Set<String> privateHandlers = new HashSet<>(this.privateHandlers);
            for (String privId : privateHandlers) {
                removePrivateHandler(privId);
            }
        }
    }
}

