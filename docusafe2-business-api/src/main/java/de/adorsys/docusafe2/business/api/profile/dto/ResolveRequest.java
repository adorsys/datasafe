package de.adorsys.docusafe2.business.api.profile.dto;

import de.adorsys.docusafe2.business.api.profile.ProfileLocationResolver;
import de.adorsys.docusafe2.business.api.types.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class ResolveRequest {

    @NonNull
    private final UserId userId;

    @NonNull
    private final ProfileLocationResolver resolver;
}
