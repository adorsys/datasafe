import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;

import javax.inject.Inject;

@RuntimeDelegate
public final class SimpleClassFinal {

    private final Dependency dependency;

    @Inject
    public SimpleClassFinal(Dependency dependency) {
        this.dependency = dependency;
    }

    public boolean isPositive() {
        return dependency.getInternalValue() > 0;
    }

    public static class Dependency {
        private final int internalValue;

        public Dependency(int internalValue) {
            this.internalValue = internalValue;
        }

        public int getInternalValue() {
            return internalValue;
        }
    }
}
