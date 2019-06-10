import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;

@RuntimeDelegate
public class SimpleClassNoInject {

    private final Dependency dependency;

    public SimpleClassNoInject(Dependency dependency) {
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
