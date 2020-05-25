package org.openhab.automation.module.script.extension.provider;

import org.eclipse.jdt.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class SuspendableFixedProvider<@NonNull E> extends FixedProvider<E> {
    private AtomicBoolean suspended = new AtomicBoolean();

    public SuspendableFixedProvider(Collection<E> all) {
        super(all);
    }

    @Override
    public Collection<E> getAll() {
        return suspended.get() ? Collections.emptyList() : super.getAll();
    }

    public boolean suspend() {
        boolean updated = suspended.compareAndSet(false, true);

        if(updated){
            super.getAll().forEach(this::notifyListenersAboutRemovedElement);
        }

        return updated;
    }

    public boolean resume() {
        boolean updated = suspended.compareAndSet(true, false);

        if(updated){
            super.getAll().forEach(this::notifyListenersAboutAddedElement);
        }

        return updated;
    }
}
