package de.adorsys.datasafe.rest.impl.config;

import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SingleDfsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String datasafeSingleStorage = System.getenv("DATASAFE_SINGLE_STORAGE");
        if (StringUtils.isNotBlank(datasafeSingleStorage)) {
            return Boolean.parseBoolean(datasafeSingleStorage);
        }
        return false;
    }
}
