import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import java.lang.Override;
import java.util.function.Function;
import javax.annotation.Generated;
import javax.annotation.Nullable;
import javax.inject.Inject;

@Generated("de.adorsys.datasafe.runtimedelegate.RuntimeDelegateGenerator")
public class SimpleClassWithVoidRuntimeDelegatable extends SimpleClassWithVoid {
    private final SimpleClassWithVoid delegate;

    @Inject
    public SimpleClassWithVoidRuntimeDelegatable(@Nullable OverridesRegistry context,
                                                 SimpleClassWithVoid.Dependency dependency) {
        super(dependency);
        ArgumentsCaptor argumentsCaptor = new ArgumentsCaptor(dependency);
        delegate = context != null ? context.findOverride(SimpleClassWithVoid.class, argumentsCaptor) : null;
    }

    @Override
    public void doCheck() {
        if (null == delegate) {
            super.doCheck();
        } else {
            delegate.doCheck();
        }
    }

    public static void overrideWith(OverridesRegistry context,
                                    Function<ArgumentsCaptor, SimpleClassWithVoid> ctorCaptor) {
        context.override(SimpleClassWithVoid.class, args -> ctorCaptor.apply((ArgumentsCaptor) args));
    }

    public static class ArgumentsCaptor {
        private final SimpleClassWithVoid.Dependency dependency;

        private ArgumentsCaptor(SimpleClassWithVoid.Dependency dependency) {
            this.dependency = dependency;
        }

        public SimpleClassWithVoid.Dependency getDependency() {
            return dependency;
        }
    }
}