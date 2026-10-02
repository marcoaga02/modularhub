package com.marcoaga02.modularhub.core.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "languages")
public class Language extends BaseEntity {

    @Override
    protected Class<? extends BaseEntity> getEntityClass() {
        return Language.class;
    }

    @Column(name = "code")
    private String code;

    @Column(name = "label")
    private String label;

    @Column(name = "is_default")
    private Boolean isDefault;

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
}
