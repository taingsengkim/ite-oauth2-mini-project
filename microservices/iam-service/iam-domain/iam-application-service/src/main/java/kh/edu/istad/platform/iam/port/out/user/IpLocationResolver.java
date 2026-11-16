package kh.edu.istad.platform.iam.port.out.user;

public interface IpLocationResolver {
    enum Status { RESOLVED, LOCAL, PRIVATE, UNAVAILABLE }
    record Location(Status status, String city, String countryCode) {}
    Location resolve(String address);
}
