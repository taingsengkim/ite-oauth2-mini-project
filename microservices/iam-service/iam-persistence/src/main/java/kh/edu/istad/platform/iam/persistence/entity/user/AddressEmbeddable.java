package kh.edu.istad.platform.iam.persistence.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class AddressEmbeddable {

    @Column(name = "address_street_address", length = 255)
    private String streetAddress;

    @Column(name = "address_locality", length = 100)
    private String locality;

    @Column(name = "address_region", length = 100)
    private String region;

    @Column(name = "address_postal_code", length = 20)
    private String postalCode;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "address_country", length = 2)
    private String country;
}