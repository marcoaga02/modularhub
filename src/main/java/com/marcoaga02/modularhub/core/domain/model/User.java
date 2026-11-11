package com.marcoaga02.modularhub.core.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Column(name = "identity_id", unique = true)
    private String identityId;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private Gender gender;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "tax_id_number")
    private String taxIdNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "username")
    private String username;

    @Column(name = "enabled")
    private Boolean enabled;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "preferences_id", unique = true)
    private AccountPreferences preferences;

    @Override
    protected Class<? extends BaseEntity> getEntityClass() {
        return User.class;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }
}
