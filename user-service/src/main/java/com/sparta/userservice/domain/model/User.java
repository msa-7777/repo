package com.sparta.userservice.domain.model;

import com.sparta.userservice.global.entity.BaseEntity;
import com.sparta.userservice.presentation.dto.request.UserDeleteRequest;
import com.sparta.userservice.presentation.dto.request.UserSignupRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Entity
@Table(name = "p_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(name = "login_id", nullable = false, unique = true, length = 10, updatable = false)
    private String loginId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "role", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "signup_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SignupStatus signupStatus;

    @Column(name = "slack_id", length = 64)
    private String slackId;

    @Column(name = "hub_id")
    private UUID hubId;

    @Column(name = "supplier_id")
    private UUID supplierId;



    private static User create(UserSignupRequest request, PasswordEncoder passwordEncoder, SignupStatus signupStatus) {
        User user = new User();
        user.loginId = request.getLoginId();
        user.password = passwordEncoder.encode(request.getPassword());
        user.name = request.getName();
        user.phone = request.getPhone();
        user.email = request.getEmail();
        user.slackId = request.getSlackId();
        user.role = request.getRole();
        user.signupStatus = signupStatus;
        user.hubId = request.getHubId();
        user.supplierId = request.getSupplierId();
        return user;
    }


    public static User createBySignup(UserSignupRequest request, PasswordEncoder passwordEncoder) {
        return create(request, passwordEncoder, SignupStatus.PENDING);
    }

    public static User createByAdmin(UserSignupRequest request, PasswordEncoder passwordEncoder) {
        return create(request, passwordEncoder, SignupStatus.APPROVED);
    }


    public void approveSignup() { this.signupStatus = SignupStatus.APPROVED; }
    public void rejectSignup() { this.signupStatus = SignupStatus.REJECTED; }


    public void changeLoginId(String newLoginId) { this.loginId = newLoginId; }
    public void changePassword(String newPassword, PasswordEncoder passwordEncoder) { this.password = passwordEncoder.encode(newPassword); };
    public void changeName(String newName) { this.name = newName; }
    public void changeEmail(String newEmail) { this.email = newEmail; }
    public void changePhone(String newPhone) { this.phone = newPhone; }
    public void changeRole(Role newRole) { this.role = newRole; }
    public void changeSignupStatus(SignupStatus newSignupStatus) { this.signupStatus = newSignupStatus; }
    public void changeSlackId(String newSlackId) { this.slackId = newSlackId; }
    public void changeHubId(UUID newHubId) { this.hubId = newHubId; }
    public void changeSupplierId(UUID newSupplierId) { this.supplierId = newSupplierId; }


    public boolean verifyCredentialsForDelete(UserDeleteRequest deleteUser, PasswordEncoder passwordEncoder) {
        if (!this.email.equals(deleteUser.getEmail())) {
            return false;
        }
        if (!passwordEncoder.matches(deleteUser.getPassword(), this.password)) {
            return false;
        }

        return true;
    }
}