package kh.edu.istad.platform.iam.rest.identity.mapper;

import kh.edu.istad.platform.iam.dto.identity.IdentityResult;
import kh.edu.istad.platform.iam.rest.identity.dto.IdentityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface IdentityApiMapper {

    IdentityResponse toResponse(IdentityResult result);

    List<IdentityResponse> toResponses(List<IdentityResult> results);
}
