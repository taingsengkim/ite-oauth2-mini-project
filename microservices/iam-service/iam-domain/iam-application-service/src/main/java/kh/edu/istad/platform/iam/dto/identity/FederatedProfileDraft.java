package kh.edu.istad.platform.iam.dto.identity;

import kh.edu.istad.platform.iam.model.IdentityProvider;

public record FederatedProfileDraft(String proofToken, IdentityProvider provider, String email,
                                    String givenName, String familyName, String locale) {}
