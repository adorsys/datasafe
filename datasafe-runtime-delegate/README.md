# Datasafe runtime-delegate

This module provides annotation processor that converts 
[@RuntimeDelegate](../datasafe-types-api/src/main/java/de/adorsys/datasafe/types/api/context/annotations/RuntimeDelegate.java)
annotated classes into delegates that check if 
[OverridesRegistry](../datasafe-types-api/src/main/java/de/adorsys/datasafe/types/api/context/overrides/OverridesRegistry.java)
contain overriding class and if it does - use override, otherwise - default implementation.
For example, 
```java
@RuntimeDelegate
class PathChecker {
    private final PathWithoutQuoutes check;
    
    @Inject
    PathChecker(PathWithoutQuoutes check) {
        this.check = check;    
    }
    
    public boolean doCheck(Path path) {
        return check.validate(path);
    }
}
```
will get following override:
```java
@Generated("de.adorsys.datasafe.runtimedelegate.RuntimeDelegateGenerator")
public class PathCheckerRuntimeDelegatable extends PathChecker {
    private final PathChecker delegate;

    @Inject
    public PrivateSpaceServiceImplRuntimeDelegatable(@Nullable OverridesRegistry context, PathWithoutQuoutes check) {
        super(check);
        ArgumentsCaptor argumentsCaptor = new ArgumentsCaptor(check);
        delegate = context != null ? context.findOverride(PathChecker.class, argumentsCaptor) : null;
    }
    
    @Override
    public boolean doCheck(Path path) {
        if (null == delegate) {
            return super.doCheck(path);
        } else {
            return delegate.doCheck(path);
        }
    }

    public static void overrideWith(OverridesRegistry context,
            Function<ArgumentsCaptor, PathChecker> ctorCaptor) {
        context.override(PathChecker.class, args -> ctorCaptor.apply((ArgumentsCaptor) args));
    }

    public static class ArgumentsCaptor {
        private final PathWithoutQuoutes check;

        private ArgumentsCaptor(PathWithoutQuoutes check) {
            this.check = check;
        }

        public PathWithoutQuoutes getCheck() {
            return check;
        }
    }
}
```
After such manipulation it is possible to use it in Dagger module, so that `PathCheckerRuntimeDelegatable` will
check if there exists an override in `OverridesRegistry` and if it does - it will call the override, otherwise it 
will call `PathChecker` default implementation.