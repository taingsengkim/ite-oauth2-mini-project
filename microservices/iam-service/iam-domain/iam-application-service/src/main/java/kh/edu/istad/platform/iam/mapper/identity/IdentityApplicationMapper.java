package kh.edu.istad.platform.iam.mapper.identity;

import kh.edu.istad.platform.iam.dto.identity.IdentityResult;
import kh.edu.istad.platform.iam.model.Identity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IdentityApplicationMapper {

    IdentityResult toResult(Identity identity);

    List<IdentityResult> toResults(List<Identity> identities);
}