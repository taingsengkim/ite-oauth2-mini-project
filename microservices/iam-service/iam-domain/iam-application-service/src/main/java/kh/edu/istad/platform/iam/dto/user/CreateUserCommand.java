package kh.edu.istad.platform.iam.dto.user;

import kh.edu.istad.platform.iam.valueobject.Address;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateUserCommand(
        String username,
        String email,
        String password,
        String givenName,
        String familyName,
        String gender,
        LocalDate birthdate,
        String phoneNumber,
        String zoneinfo,
        String locale,
        Address address,
        List<UUID> roleUuids
) {

    @Override
    public String toString() {
        return "CreateUserCommand[username=" + username + "]";
    }
}
