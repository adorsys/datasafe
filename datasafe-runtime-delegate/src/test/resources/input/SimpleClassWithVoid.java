import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;

import javax.inject.Inject;

@RuntimeDelegate
public class SimpleClassWithVoid {

    private final Dependency dependency;

    @Inject
    public SimpleClassWithVoid(Dependency dependency) {
        this.dependency = dependency;
    }

    public void doCheck() {
        dependency.doCheck();
    }

    public static class Dependency {
        private final int internalValue;

        public Dependency(int internalValue) {
            this.internalValue = internalValue;
        }

        public void doCheck() {
        }
    }
}
