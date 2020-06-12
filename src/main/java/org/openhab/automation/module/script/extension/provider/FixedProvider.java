package org.openhab.automation.module.script.extension.provider;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.registry.AbstractProvider;

import java.util.ArrayList;
import java.util.Collection;

public class FixedProvider<@NonNull E> extends AbstractProvider<E> {
    private Collection<E> all;

    public FixedProvider(Collection<E> all) {
        this.all = new ArrayList<>(all);
    }

    @Override
    public Collection<E> getAll() {
        return all;
    }
}
