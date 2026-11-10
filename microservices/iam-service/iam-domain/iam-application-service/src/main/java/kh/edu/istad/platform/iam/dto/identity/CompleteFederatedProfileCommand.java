package kh.edu.istad.platform.iam.dto.identity;

public record CompleteFederatedProfileCommand(String proofToken, String email, String givenName,
                                              String familyName, String locale) {
    @Override
    public String toString() { return "CompleteFederatedProfileCommand[redacted]"; }
}
