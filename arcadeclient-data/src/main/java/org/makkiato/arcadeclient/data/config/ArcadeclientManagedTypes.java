package org.makkiato.arcadeclient.data.config;

import org.springframework.data.domain.ManagedTypes;

import java.util.function.Consumer;

public class ArcadeclientManagedTypes implements ManagedTypes {
    private final ManagedTypes delegate;

    public ArcadeclientManagedTypes(ManagedTypes types) {
        this.delegate = types;
    }

    /**
     * Applies the given {@link Consumer action} to each of the {@link Class types} contained in this
     * {@link ManagedTypes}
     * instance.
     *
     * @param action {@link Consumer} defining the action to perform on the {@link Class types} contained in this
     *               {@link ManagedTypes} instance; must not be {@literal null}.
     * @see Consumer
     */
    @Override
    public void forEach(Consumer<Class<?>> action) {
        delegate.forEach(action);
    }

    public static ArcadeclientManagedTypes from(ManagedTypes managedTypes) {
        return new ArcadeclientManagedTypes(managedTypes);
    }

    public static ArcadeclientManagedTypes fromIterable(Iterable<? extends Class<?>> types) {
        return from(ManagedTypes.fromIterable(types));
    }
}
