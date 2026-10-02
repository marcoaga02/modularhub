package com.marcoaga02.modularhub.modules.usermanagement.domain.repository.specification;

import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> byCriteria(UserCriteriaDTO criteria) {
        Specification<User> spec = Specification.where(notDeleted());

        if (criteria == null) {
            return spec;
        }

        if (StringUtils.hasText(criteria.getText())) {
            spec = spec.and(textSearch(criteria.getText()));
        }

        return spec;
    }

    private static Specification<User> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedOn"));
    }

    private static Specification<User> textSearch(String text) {
        String pattern = "%" + text.toLowerCase().trim() + "%";

        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstname")), pattern),
                cb.like(cb.lower(root.get("lastname")), pattern),
                cb.like(cb.lower(root.get("email")), pattern)
        );
    }

}
