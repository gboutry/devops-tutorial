package org.boutry.devops.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;


//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.IntSequenceGenerator.class,
//        property="id")
@Entity
public class UserEntity extends PanacheEntity implements Serializable {

    @NotNull(message = "Firstname must not be null")
    @NotBlank(message = "Firstname must not be blank")
    public String firstname;
    @NotNull(message = "Lastname must not be null")
    @NotBlank(message = "Lastname must not be blank")
    public String lastname;
    @NotNull(message = "Email must not be null")
    @NotBlank(message = "Email must not be blank")
    @Email
    @Column(unique = true)
    public String email;

    public UserEntity() {

    }

    public UserEntity(String firstname, String lastname, String email) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public static UserEntity findById(long id) {
        return find("id", id).firstResult();
    }

    public static Optional<UserEntity> findByIdOptional(long id) {
        return Optional.ofNullable(UserEntity.findById(id));
    }

    public static Optional<UserEntity> findByEmailOptional(@NotNull String email) {
        return Optional.ofNullable(UserEntity.find("email", email).firstResult());
    }

    public static void delete(UserEntity user) {
        delete("id", user.id);
    }

    public String getFirstname() {
        return this.firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    @Override
    public void persist() {
        Optional<UserEntity> user = UserEntity.findByEmailOptional(email);
        if (user.isPresent()) {
            throw new ConstraintViolationException("email should be unique", null);
        }
        super.persist();
    }

    @Override
    public void persistAndFlush() {
        Optional<UserEntity> user = UserEntity.findByEmailOptional(email);
        if (user.isPresent()) {
            throw new ConstraintViolationException("email should be unique", null);
        }
        super.persistAndFlush();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return id.equals(that.id) &&
                firstname.equals(that.firstname) &&
                lastname.equals(that.lastname) &&
                email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, lastname, email);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
