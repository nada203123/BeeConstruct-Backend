package com.example.users.controllers.Users;

import com.example.users.dto.request.*;
import com.example.users.model.Users.User;
import com.example.users.repositories.Users.UserRepository;
import com.example.users.services.Users.FileStorageService;
import com.example.users.services.Users.UserServiceImpl;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserServiceImpl userService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public UserController(UserServiceImpl userService, UserRepository userRepository,FileStorageService fileStorageService ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }


    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }



    @PostMapping

    public ResponseEntity<?> createUser(@Valid @RequestBody registerRequest request) {
        try {
            User user = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (ResponseStatusException e) {
            String errorMessage = e.getReason();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", errorMessage);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "An error occurred while registering the user.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody loginRequest loginRequest) {
        return userService.login(loginRequest);
    }


    @PostMapping("/roles")
    public ResponseEntity<String> getRoleByEmail(@Valid @RequestBody getRoleRequest request) {
        try {
            String role = userService.getUserRoleFromDb(request);
            return ResponseEntity.ok(role);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<User> archiveUser(@PathVariable Long id) {
        User archivedUser = userService.archiveUser(id);
        return ResponseEntity.ok(archivedUser);
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<User> restoreUser(@PathVariable Long id) {
        User restoredUser = userService.restoreUser(id);
        return ResponseEntity.ok(restoredUser);
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "User deleted successfully."));

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user.");
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody forgotPasswordRequest request) {

        userService.forgotPassword(request);
        return ResponseEntity.ok("Verification code sent to email.");
    }


    @PutMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp( @RequestBody verifyCodeRequest request) {
        try {
            boolean isValid = userService.verifyOtp(request);
            return ResponseEntity.ok("OTP verified.");
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                // Handle expired OTP case
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP expired.");
            } else if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Handle invalid OTP case
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP.");
            } else {
                // Handle other cases
                return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
            }
        } catch (RuntimeException ex) {
            // Handle other runtime exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + ex.getMessage());
        }
    }

    @PostMapping("/regenerate-otp")
    public ResponseEntity<String> regenerateOtpCode(@RequestBody regenerateCodeRequest request) {


        return userService.regenerateOtpCode(request);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody resetPasswordRequest request) {

        return userService.resetPassword( request);
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable String id) {

        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserRoles(id));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refresh_token");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token is required.");
        }

        return userService.logout(refreshToken);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> modifyUser(@PathVariable String id, @RequestBody modifyUserRequest modifyUserRequest) {
        User updatedUser = userService.modifyUser(id, modifyUserRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping(value = "/{userId}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> modifyProfile(
            @PathVariable Long userId,
            @ModelAttribute modifyProfileRequest modifyProfileRequest

    ) {
        User updatedUser = userService.modifyProfile(userId, modifyProfileRequest);
        return ResponseEntity.ok(updatedUser);
    }





    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody modifyPasswordRequest request) {
        userService.updatePassword(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password updated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<User>> getAllActiveUsers() {
        List<User> activeUsers = userService.getAllActiveUsers();
        return ResponseEntity.ok(activeUsers);
    }

    @GetMapping("/archived")
    public ResponseEntity<List<User>> getAllArchivedUsers() {
        List<User> archivedUsers = userService.getAllArchivedUsers();
        return ResponseEntity.ok(archivedUsers);
    }

    @GetMapping("/{userId}/photo")
    public ResponseEntity<Resource> getUserPhoto(@PathVariable Long userId) {
        try {
            // 1. Récupération de l'utilisateur
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // 2. Vérifier s'il a une photo de profil enregistrée
            String filename = user.getProfilePhotoPath();
            if (filename == null || filename.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User has no profile photo");
            }

            // 3. Récupération du fichier depuis MinIO (ou autre service)
            InputStream fileStream = fileStorageService.getFile(filename);
            Resource fileResource = new InputStreamResource(fileStream);

            // 4. Déduction du type MIME (optionnellement dynamique)
            String contentType = "image/jpeg"; // Par défaut
            if (filename.endsWith(".png")) contentType = "image/png";
            else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (filename.endsWith(".gif")) contentType = "image/gif";

            // 5. Retourner la ressource
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileResource);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving photo", e);
        }
    }



   /* @PutMapping(value = "/profile/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateProfile(
            @PathVariable String userId,
            @ModelAttribute modifyProfileRequest request
    ) {
        User updatedUser = userService.modifyProfile(userId, request);
        return ResponseEntity.ok(updatedUser);
    }*/






}
