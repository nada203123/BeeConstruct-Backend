package com.example.users.services.Users;

import com.example.users.dto.request.*;
import com.example.users.mail.EmailService;
import com.example.users.model.Users.User;
import com.example.users.repositories.Users.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Value("${app.keycloak.realm}")
    private String realm;

    private final Keycloak keycloak;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String KEYCLOAK_TOKEN_URL = "https://keycloak.dpc.com.tn:8443/realms/BeeConstruct/protocol/openid-connect/token";
    private static final String CLIENT_ID = "BeeConstructBack"; //
    private static final String CLIENT_SECRET = "YMzciivjvgqwyVLavpYKcWbjL5QHwgRr";
    private final EmailService emailService;
    private final FileStorageService fileStorageService;

    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository , Keycloak keycloak ,EmailService emailService,PasswordEncoder passwordEncoder,  FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.keycloak = keycloak;
        this.emailService =  emailService;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;

    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserResource getUser(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }


    private UsersResource getUsersResource(){

        return keycloak.realm(realm).users();
    }


    @Override
    public User  createUser(@Valid registerRequest request) {
       // Map<String, String> responseE = new HashMap<>();

        validateUserDoesNotExist(request);

        int randomCode = new Random().nextInt(9000) + 1000;
        String generatedUsername = request.getFirstName().toLowerCase() + "." + request.getLastName().toLowerCase() + "_" + randomCode;

        String plainPassword = request.getPassword();
        String hashedPassword = passwordEncoder.encode(request.getPassword());

// Convert DTO to Entity
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(hashedPassword)
                .role(request.getRole())
                .archived(false)
                .build();

        User savedUser = userRepository.save(user);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(generatedUsername); // Required field
        userRepresentation.setFirstName(request.getFirstName());
        userRepresentation.setLastName(request.getLastName());
        userRepresentation.setEmail(request.getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(false);

        CredentialRepresentation credentials=new CredentialRepresentation();
        credentials.setValue(plainPassword);
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setTemporary(false);
        userRepresentation.setCredentials(List.of(credentials));

        //UsersResource is a Keycloak client resource that allows you to manage users in a particular realm in Keycloak. It provides methods to perform operations like creating users, getting user details, updating users, and deleting users.

        UsersResource usersResource = getUsersResource();

        Response response = usersResource.create(userRepresentation);

        log.info("Status Code "+response.getStatus());

        if(!Objects.equals(201,response.getStatus())){

            throw new RuntimeException("Status code "+response.getStatus());
        }

        log.info("New user has bee created");

        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(generatedUsername, true);
        UserRepresentation createdUser = userRepresentations.get(0);
        String keycloakId = createdUser.getId();

        savedUser.setKeycloakId(keycloakId);
        userRepository.save(savedUser);

        assignRole(keycloakId, request.getRole());

        emailService.accountVerification( user.getEmail(), plainPassword);


        return savedUser;


    }

    private void validateUserDoesNotExist(registerRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already exists");
        }
    }

    public void assignRole(String userId, String roleName) {


        UserResource user = getUser(userId);
        RolesResource rolesResource = getRolesResource();
        RoleRepresentation representation = rolesResource.get(roleName).toRepresentation();
        user.roles().realmLevel().add(Collections.singletonList(representation));
    }


    @Override
    public ResponseEntity<String> login( loginRequest loginRequest) {

        //400
        if (!loginRequest.getEmail().matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {

            return createErrorResponse("INVALID_EMAIL_FORMAT",
                    "Format d'e-mail invalide",
                    HttpStatus.BAD_REQUEST);
        }

        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {

            return createErrorResponse("EMAIL_REQUIRED",
                    "Adresse e-mail est requise",
                    HttpStatus.BAD_REQUEST);

        }

        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            return createErrorResponse("EMAIL_NOT_FOUND",
                    "Adresse e-mail non trouvée",
                    HttpStatus.UNAUTHORIZED);
        }

        // Check if user is archived
        User user =  userOptional.get();

        if (user.isArchived()) {

            return createErrorResponse("USER_ARCHIVED",
                    "Compte utilisateur archivé. Veuillez contacter l'administrateur.",
                    HttpStatus.FORBIDDEN);
        }

        if (user.getKeycloakId() != null) {
            try {
                UserResource userResource = getUsersResource().get(user.getKeycloakId());
                UserRepresentation userRepresentation = userResource.toRepresentation();
                if (!userRepresentation.isEnabled()) {


                    return createErrorResponse("USER_DISABLED",
                            "Compte utilisateur désactivé. Veuillez contacter l'administrateur.",
                            HttpStatus.FORBIDDEN);


                }
            } catch (Exception e) {
                log.error("Error checking user status in Keycloak: {}", e.getMessage());
            }
        }

        return authenticateWithKeycloak(loginRequest);



    }

    private ResponseEntity<String> authenticateWithKeycloak(loginRequest loginRequest) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", CLIENT_ID);
        map.add("client_secret", "YMzciivjvgqwyVLavpYKcWbjL5QHwgRr");
        map.add("grant_type", "password");
        map.add("username", loginRequest.getEmail());
        map.add("password", loginRequest.getPassword());


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    KEYCLOAK_TOKEN_URL, HttpMethod.POST, request, String.class);
            log.info("Response: " + response.getBody());

            return response;
        } catch (HttpClientErrorException e) {
            String errorMessage = "Informations d'identification invalides";
            String errorCode = "INVALID_CREDENTIALS";


            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                errorMessage = " Mot de passe incorrect";
                errorCode = "INCORRECT_PASSWORD";
            }

            return createErrorResponse(errorCode, errorMessage, HttpStatus.UNAUTHORIZED);
        }

    }



    private ResponseEntity<String> createErrorResponse(String errorCode, String errorMessage, HttpStatus status) {
        Map<String, String> errorResponse = Map.of(
                "error", errorCode,
                "message", errorMessage
        );
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResponse);
        } catch (JsonProcessingException e) {
            log.error("Error converting response to JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"INTERNAL_ERROR\",\"message\":\"Error processing response\"}");
        }
    }






    @Override
    public void forgotPassword(forgotPasswordRequest request) {

        User user =  userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found");
                });

        if (user.isArchived()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Compte utilisateur archivé. Veuillez contacter l'administrateur.");
        }


        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        user.setOtpCode(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(3));
        userRepository.save(user);

        emailService.sendOtpVerification(user.getEmail(), otp);

    }

@Override
    public boolean verifyOtp(verifyCodeRequest request) {

    User user =  userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

    System.out.println("Stored OTP: '" + user.getOtpCode() + "'");
    System.out.println("Received OTP: '" + request.getOtpCode() + "'");

    if (user.getOtpExpiration().isBefore(LocalDateTime.now())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired.");
    }

    if (!user.getOtpCode().equals(request.getOtpCode())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP.");
    }


    return user.getOtpCode().equals(request.getOtpCode());
    }
@Override
    public ResponseEntity<String> regenerateOtpCode(regenerateCodeRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpCode() != null && user.getOtpExpiration().isAfter(LocalDateTime.now())) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("OTP is still valid. Please verify the current OTP.");
        }

    String otp = String.valueOf(new Random().nextInt(900000) + 100000);

    user.setOtpCode(otp);
    user.setOtpExpiration(LocalDateTime.now().plusMinutes(3));
    userRepository.save(user);

    try {
        emailService.sendOtpVerification(user.getEmail(), otp);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to send OTP. Please try again later.");
    }

    return ResponseEntity.ok("A new OTP has been sent to your email.");

    }

    @Override
    public ResponseEntity<String> resetPassword( resetPasswordRequest request) {
        String email = request.getEmail();

        User user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Passwords do not match.");
        }

        UsersResource usersResource = keycloak.realm(realm).users();
        List<UserRepresentation> userRepresentations = usersResource.searchByEmail(email, true);

        if (userRepresentations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found in Keycloak.");
        }

        String userId = userRepresentations.get(0).getId();
        UserResource userResource = usersResource.get(userId);

        // Set new password in Keycloak
        CredentialRepresentation newCredential = new CredentialRepresentation();
        newCredential.setType(CredentialRepresentation.PASSWORD);
        newCredential.setValue(request.getNewPassword());
        newCredential.setTemporary(false);

        userResource.resetPassword(newCredential);

        //user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPassword(request.getNewPassword());


        user.setOtpCode(null);
        user.setOtpExpiration(null);

        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully.");
    }


    @Override
    public List<RoleRepresentation> getUserRoles(String userId) {


        return getUser(userId).roles().realmLevel().listAll();
    }

    @Override
    public String getUserRoleFromDb(getRoleRequest request) {
        // Validate request (Jakarta validation will handle @NotBlank automatically)
        User user = (User) userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        log.info("Found user: {}", user);

        return user.getRole();
    }


    @Override
    public void deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        log.info(String.valueOf(userOptional));

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Delete from Keycloak if we have a Keycloak ID
            if (user.getKeycloakId() != null && !user.getKeycloakId().isEmpty()) {
                try {
                    UsersResource usersResource = getUsersResource();
                    usersResource.delete(user.getKeycloakId());
                    log.info("User deleted from Keycloak with ID: {}", user.getKeycloakId());
                } catch (Exception e) {
                    log.error("Failed to delete user from Keycloak: {}", e.getMessage());

                }
            }

            // Delete from your database
            userRepository.deleteById(userId);
            log.info("User deleted from database with ID: {}", userId);
        } else {
            log.warn("User with ID {} not found in the database.", userId);
            throw new EntityNotFoundException("User with ID " + userId + " not found");
        }
    }





    @Override
    public User updatePassword(modifyPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }


        User user =  userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }


        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());


        user.setPassword(hashedNewPassword);
        User updatedUser = userRepository.save(user);


        try {

            UsersResource usersResource = keycloak.realm(realm).users();
            List<UserRepresentation> userRepresentations = usersResource.searchByEmail(request.getEmail(), true);

            if (userRepresentations.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in Keycloak");
            }

            String userId = userRepresentations.get(0).getId();
            UserResource userResource = usersResource.get(userId);


            CredentialRepresentation newCredential = new CredentialRepresentation();
            newCredential.setType(CredentialRepresentation.PASSWORD);
            newCredential.setValue(request.getNewPassword());
            newCredential.setTemporary(false);

            userResource.resetPassword(newCredential);

            return updatedUser;
        } catch (Exception e) {
            // Rollback local database password update if Keycloak update fails
            user.setPassword(user.getPassword());
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update password", e);
        }
    }


    private RolesResource getRolesResource(){

        return keycloak.realm(realm).roles();
    }

    @Override
    public ResponseEntity<String> logout(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Construct the request body
        String body = "client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&refresh_token=" + refreshToken;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        // Keycloak logout URL
        String logoutUrl = "https://keycloak.dpc.com.tn/realms/" + realm + "/protocol/openid-connect/logout";

        try {
            // Send the logout request to Keycloak
            ResponseEntity<String> response = restTemplate.exchange(
                    logoutUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return ResponseEntity.ok("Logged out successfully.");
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to logout.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during logout: " + e.getMessage());
        }
    }



    @Override
    public User modifyUser(@Valid String userId, modifyUserRequest modifyUserRequest) {
        // Find the user by ID from the database
        User user = userRepository.findByKeycloakId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check if the email or phone number already exists (if changed)
        if (!user.getEmail().equals(modifyUserRequest.getEmail()) &&
                userRepository.findByEmail(modifyUserRequest.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        if (!user.getPhoneNumber().equals(modifyUserRequest.getPhoneNumber()) &&
                userRepository.findByPhoneNumber(modifyUserRequest.getPhoneNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already exists");
        }

        // Update the user's information
        if (modifyUserRequest.getFirstName() != null) {
            user.setFirstName(modifyUserRequest.getFirstName());
        }
        if (modifyUserRequest.getLastName() != null) {
            user.setLastName(modifyUserRequest.getLastName());
        }
        if (modifyUserRequest.getEmail() != null) {
            user.setEmail(modifyUserRequest.getEmail());
        }
        if (modifyUserRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(modifyUserRequest.getPhoneNumber());
        }
        if (modifyUserRequest.getRole() != null) {
            user.setRole(modifyUserRequest.getRole());
        }



        // If a password is provided, hash it before updating
        if (modifyUserRequest.getPassword() != null && !modifyUserRequest.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(modifyUserRequest.getPassword());
            user.setPassword(hashedPassword);
        }

        // Save the updated user in the database
        User updatedUser = userRepository.save(user);

        // Update user information in Keycloak
        UsersResource usersResource = getUsersResource();

        // Fetch the user from Keycloak using the Keycloak user ID (userId)
        UserResource userResource = usersResource.get(userId);
        UserRepresentation userRepresentation = userResource.toRepresentation();

        // Update user information in Keycloak
        if (modifyUserRequest.getFirstName() != null) {
            userRepresentation.setFirstName(modifyUserRequest.getFirstName());
        }
        if (modifyUserRequest.getLastName() != null) {
            userRepresentation.setLastName(modifyUserRequest.getLastName());
        }
        if (modifyUserRequest.getEmail() != null) {
            userRepresentation.setEmail(modifyUserRequest.getEmail());
        }
        if (modifyUserRequest.getFirstName() != null && modifyUserRequest.getLastName() != null) {
            userRepresentation.setUsername(modifyUserRequest.getFirstName() + modifyUserRequest.getLastName() + "_" + userId.substring(0, 4));
        }
        // Save the updated user representation in Keycloak
        userResource.update(userRepresentation);



        // If a password is provided, update it in Keycloak
        if (modifyUserRequest.getPassword() != null && !modifyUserRequest.getPassword().isEmpty()) {
            CredentialRepresentation newCredential = new CredentialRepresentation();
            newCredential.setType(CredentialRepresentation.PASSWORD);
            newCredential.setValue(modifyUserRequest.getPassword());
            newCredential.setTemporary(false);
            userResource.resetPassword(newCredential);
        }


        // Handle role updates in Keycloak
        String newRole = modifyUserRequest.getRole();
        String oldRole = user.getRole();

        if (oldRole != null && !oldRole.equals(newRole)) {
            // Remove the old role from Keycloak
            List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll();
            RoleRepresentation oldRoleRepresentation = currentRoles.stream()
                    .filter(role -> role.getName().equals(oldRole))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Old role not found in Keycloak"));

            userResource.roles().realmLevel().remove(Collections.singletonList(oldRoleRepresentation));

            // Assign the new role in Keycloak
            if (newRole != null && !newRole.isEmpty()) {
                assignRole(userId, newRole);
            }
        }

        try {
            emailService.modifyUserEmail(
                    modifyUserRequest.getEmail(), // Email
                    modifyUserRequest.getFirstName(), // First name
                    modifyUserRequest.getLastName(), // Last name
                    modifyUserRequest.getPassword(), // Password
                    modifyUserRequest.getPhoneNumber(),
                    modifyUserRequest.getRole() // Role
            );
        } catch (Exception e) {
            // Log the error but do not stop the process
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return updatedUser;
    }



    @Override
    public User modifyProfile(Long userId, modifyProfileRequest modifyProfileRequest) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (modifyProfileRequest.getPhoneNumber() != null &&
                !user.getPhoneNumber().equals(modifyProfileRequest.getPhoneNumber()) &&
                userRepository.findByPhoneNumber(modifyProfileRequest.getPhoneNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already exists");
        }

        if (modifyProfileRequest.getFirstName() != null && !modifyProfileRequest.getFirstName().isBlank()) {
            user.setFirstName(modifyProfileRequest.getFirstName());
        }
        if (modifyProfileRequest.getLastName() != null && !modifyProfileRequest.getLastName().isBlank()) {
            user.setLastName(modifyProfileRequest.getLastName());
        }
        if (modifyProfileRequest.getPhoneNumber() != null && !modifyProfileRequest.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(modifyProfileRequest.getPhoneNumber());
        }
        if (modifyProfileRequest.getGender() != null && !modifyProfileRequest.getGender().isBlank()) {
            user.setGender(modifyProfileRequest.getGender());
        }
        if (modifyProfileRequest.getCountry() != null && !modifyProfileRequest.getCountry().isBlank()) {
            user.setCountry(modifyProfileRequest.getCountry());
        }

        if (modifyProfileRequest.getImage() != null && !modifyProfileRequest.getImage().isEmpty()) {
            try {
                MultipartFile imageFile = modifyProfileRequest.getImage();
                String contentType = imageFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
                }
                if (imageFile.getSize() > 5 * 1024 * 1024) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must be less than 5MB");
                }

                String filename = generateUniqueFilename(String.valueOf(userId), imageFile.getOriginalFilename());
                Map<String, String> fileUploadResponse = fileStorageService.storeFile(imageFile, filename);
                String fileUrl = fileUploadResponse.get("fileUrl");

                user.setProfilePhotoPath(filename);

            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload profile photo", e);
            }
        }

        System.out.println("in the dbbbbbb");


        User updatedUser = userRepository.save(user);

        System.out.println("afterthe dbbbbbb");

        UsersResource usersResource = getUsersResource();

        System.out.println(user.getKeycloakId());

        UserResource userResource = usersResource.get(user.getKeycloakId());
        UserRepresentation userRepresentation = userResource.toRepresentation();



        if (modifyProfileRequest.getFirstName() != null && !modifyProfileRequest.getFirstName().isBlank()) {
            userRepresentation.setFirstName(modifyProfileRequest.getFirstName());
        }
        if (modifyProfileRequest.getLastName() != null && !modifyProfileRequest.getLastName().isBlank()) {
            userRepresentation.setLastName(modifyProfileRequest.getLastName());
        }

        String keycloakUserID = user.getKeycloakId();


        if (modifyProfileRequest.getFirstName() != null && !modifyProfileRequest.getFirstName().isBlank() &&
                modifyProfileRequest.getLastName() != null && !modifyProfileRequest.getLastName().isBlank()) {
            userRepresentation.setUsername(modifyProfileRequest.getFirstName() + modifyProfileRequest.getLastName() + "_" + keycloakUserID.substring(0, 4) );
        }




        userResource.update(userRepresentation);


        return updatedUser;
    }

    private String generateUniqueFilename(String userId, String originalFilename) {
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return userId + "_" + System.currentTimeMillis() + fileExtension;
    }

    @Override
    public User archiveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isArchived()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already archived");
        }

        user.setArchived(true);
        User archivedUser = userRepository.save(user);

        if (user.getKeycloakId() != null && !user.getKeycloakId().isEmpty()) {
            try {
                UserResource userResource = getUsersResource().get(user.getKeycloakId());
                UserRepresentation userRepresentation = userResource.toRepresentation();
                userRepresentation.setEnabled(false); // Disable the user in Keycloak
                userResource.update(userRepresentation);
                log.info("User disabled in Keycloak with ID: {}", user.getKeycloakId());
            } catch (Exception e) {
                log.error("Failed to disable user in Keycloak: {}", e.getMessage());
                // Consider if you want to roll back the database change on Keycloak failure
            }
        }

        return archivedUser;
    }

    @Override
    public User restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.isArchived()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not archived");
        }

        user.setArchived(false);
        User unarchivedUser = userRepository.save(user);

        if (user.getKeycloakId() != null && !user.getKeycloakId().isEmpty()) {
            try {
                UserResource userResource = getUsersResource().get(user.getKeycloakId());
                UserRepresentation userRepresentation = userResource.toRepresentation();
                userRepresentation.setEnabled(true); // Re-enable the user in Keycloak
                userResource.update(userRepresentation);
                log.info("User re-enabled in Keycloak with ID: {}", user.getKeycloakId());
            } catch (Exception e) {
                log.error("Failed to re-enable user in Keycloak: {}", e.getMessage());
            }
        }

        return unarchivedUser;
    }

    @Override
    public List<User> getAllActiveUsers() {
        return userRepository.findByArchivedFalse();
    }

    @Override
    public List<User> getAllArchivedUsers() {
        return userRepository.findByArchivedTrue();
    }
}
