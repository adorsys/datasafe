import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import java.lang.Override;
import java.util.function.Function;
import javax.annotation.Generated;
import javax.annotation.Nullable;
import javax.inject.Inject;

@Generated("de.adorsys.datasafe.runtimedelegate.RuntimeDelegateGenerator")
public class SimpleClassRuntimeDelegatable extends SimpleClass {
    private final SimpleClass delegate;

    @Inject
    public SimpleClassRuntimeDelegatable(@Nullable OverridesRegistry context,
                                         SimpleClass.Dependency dependency) {
        super(dependency);
        ArgumentsCaptor argumentsCaptor = new ArgumentsCaptor(dependency);
        delegate = context != null ? context.findOverride(SimpleClass.class, argumentsCaptor) : null;
    }

    @Override
    public boolean isPositive() {
        if (null == delegate) {
            return super.isPositive();
        } else {
            return delegate.isPositive();
        }
    }

    public static void overrideWith(OverridesRegistry context,
                                    Function<ArgumentsCaptor, SimpleClass> ctorCaptor) {
        context.override(SimpleClass.class, args -> ctorCaptor.apply((ArgumentsCaptor) args));
    }

    public static class ArgumentsCaptor {
        private final SimpleClass.Dependency dependency;

        private ArgumentsCaptor(SimpleClass.Dependency dependency) {
            this.dependency = dependency;
        }

        public SimpleClass.Dependency getDependency() {
            return dependency;
        }
    }
}