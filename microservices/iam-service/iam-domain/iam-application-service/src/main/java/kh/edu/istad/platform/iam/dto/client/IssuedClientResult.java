package kh.edu.istad.platform.iam.dto.client;

public record IssuedClientResult(ManagedClientResult client, String clientSecret) {
    @Override public String toString() {
        return "IssuedClientResult[client=" + client.clientId() + ", clientSecret=[REDACTED]]";
    }
}
