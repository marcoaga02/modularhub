package com.marcoaga02.modularhub.core.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(PROTECTED)
    private Long id;

    @Column(name = "uuid", unique = true)
    @Setter(PROTECTED)
    private String uuid;

    @Version
    @Setter(PROTECTED)
    private Long version;

    protected BaseEntity() {
        uuid = UUID.randomUUID().toString();
    }

    protected abstract Class<? extends BaseEntity> getEntityClass();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        Object unproxied = Hibernate.unproxy(obj);
        if (!(unproxied instanceof BaseEntity other)) {
            return false;
        }

        return Objects.equals(uuid, other.uuid)
                && Objects.equals(getEntityClass(), other.getEntityClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return getClass().getName() + " [id=" + id + ", uuid=" + uuid + "]";
    }
}
