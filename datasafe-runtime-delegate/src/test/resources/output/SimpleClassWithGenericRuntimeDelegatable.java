import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;

import javax.annotation.Generated;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

@Generated("de.adorsys.datasafe.runtimedelegate.RuntimeDelegateGenerator")
public class SimpleClassWithGenericRuntimeDelegatable<T extends List> extends SimpleClassWithGeneric<T> {
    private final SimpleClassWithGeneric<T> delegate;

    @Inject
    public SimpleClassWithGenericRuntimeDelegatable(@Nullable OverridesRegistry context, T list,
                                                    SimpleClassWithGeneric.Dependency dependency) {
        super(list, dependency);
        ArgumentsCaptor argumentsCaptor = new ArgumentsCaptor(list, dependency);
        delegate = context != null ? context.findOverride(SimpleClassWithGeneric.class, argumentsCaptor) : null;
    }

    @Override
    public boolean isPositive() {
        if (null == delegate) {
            return super.isPositive();
        } else {
            return delegate.isPositive();
        }
    }

    @Override
    public boolean isEmpty() {
        if (null == delegate) {
            return super.isEmpty();
        } else {
            return delegate.isEmpty();
        }
    }

    public static void overrideWith(OverridesRegistry context,
                                    Function<ArgumentsCaptor, SimpleClassWithGeneric> ctorCaptor) {
        context.override(SimpleClassWithGeneric.class, args -> ctorCaptor.apply((ArgumentsCaptor) args));
    }

    public static class ArgumentsCaptor<T extends List> {
        private final T list;

        private final SimpleClassWithGeneric.Dependency dependency;

        private ArgumentsCaptor(T list, SimpleClassWithGeneric.Dependency dependency) {
            this.list = list;
            this.dependency = dependency;
        }

        public T getList() {
            return list;
        }

        public SimpleClassWithGeneric.Dependency getDependency() {
            return dependency;
        }
    }
}