import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;

import javax.inject.Inject;
import java.util.List;

@RuntimeDelegate
public class SimpleClassWithGeneric<T extends List> {

    private final Dependency dependency;
    private final T list;

    @Inject
    public SimpleClassWithGeneric(T list, Dependency dependency) {
        this.dependency = dependency;
        this.list = list;
    }

    public boolean isPositive() {
        return dependency.getInternalValue() > 0;
    }

    public boolean isEmpty() {
        return list.isEmpty();
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
