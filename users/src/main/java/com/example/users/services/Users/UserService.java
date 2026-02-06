package com.example.users.services.Users;

import com.example.users.dto.request.*;
import com.example.users.model.Users.User;
import jakarta.validation.Valid;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();


    User getUserByEmail(String email);

    User createUser(registerRequest request);
    ResponseEntity<String> login( loginRequest loginRequest);




    String getUserRoleFromDb(getRoleRequest request);


    void forgotPassword(forgotPasswordRequest request);
    UserResource getUser(String userId);

    boolean verifyOtp(verifyCodeRequest request);

    ResponseEntity<String> regenerateOtpCode(regenerateCodeRequest request);

    ResponseEntity<String> resetPassword( resetPasswordRequest request);

    List<RoleRepresentation> getUserRoles(String userId);

    void deleteUser(Long userId);

    //User updateUserProfile(Long id, User user);
    User updatePassword(modifyPasswordRequest request);

    ResponseEntity<String> logout(String refreshToken);
    User modifyUser(@Valid String userId, modifyUserRequest modifyUserRequest);

    User modifyProfile(Long userId, modifyProfileRequest modifyProfileRequest);

    User archiveUser(Long id);

    User restoreUser(Long id);

    List<User> getAllActiveUsers();

    List<User> getAllArchivedUsers();
}
