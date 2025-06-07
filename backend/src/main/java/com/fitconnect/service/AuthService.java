package com.fitconnect.service;

import com.fitconnect.dto.LoginRequest;
import com.fitconnect.dto.LoginResponse;
import com.fitconnect.dto.RegisterRequest;
import com.fitconnect.entity.Client;
import com.fitconnect.entity.Professional;
import com.fitconnect.entity.User;
import com.fitconnect.entity.UserRole;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.util.ModularCrypt;
import org.wildfly.security.password.spec.ClearPasswordSpec;


import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String jwtIssuer;

    @Inject
    @ConfigProperty(name = "smallrye.jwt.sign.key.location")
    String privateKeyLocation;


    private PasswordFactory passwordFactory;

    public AuthService() {
        try {
            passwordFactory = PasswordFactory.getInstance(org.wildfly.security.password.interfaces.BCryptPassword.ALGORITHM_BCRYPT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PasswordFactory", e);
        }
    }

    private String hashPassword(String password) {
        // Create a clear password spec
        ClearPasswordSpec clearSpec = new ClearPasswordSpec(password.toCharArray());
        // Generate a new hashed password
        Password newPassword = null;
        try {
            newPassword = passwordFactory.generatePassword(clearSpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        // Encode it to a string that can be stored
        try {
            return Arrays.toString(ModularCrypt.encode(newPassword));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        // Decode the stored hashed password string
        Password storedPassword = null;
        try {
            storedPassword = ModularCrypt.decode(hashedPassword);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        // Verify the plain password against the stored hashed password
        try {
            return passwordFactory.verify(storedPassword, plainPassword.toCharArray());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public User registerClient(RegisterRequest request) {
        if (User.find("email", request.getEmail()).firstResultOptional().isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        Client client = new Client();
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPassword(hashPassword(request.getPassword()));
        client.setPhoneNumber(request.getPhoneNumber());
        client.persist();
        return client;
    }

    @Transactional


    public Optional<LoginResponse> login(LoginRequest request) {
        Optional<User> userOptional = User.find("email", request.getEmail()).firstResultOptional();
        LOG.infof("searching user: " + request.getEmail());
        if (userOptional.isPresent()) {
            LOG.infof("user found");
            User user = userOptional.get();
            if (verifyPassword(request.getPassword(), user.getPassword())) {
                Set<String> roles = new HashSet<>();
                roles.add(user.getRole().name());

                String token = Jwt.issuer(jwtIssuer)
                                  .upn(user.getEmail()) // User Principal Name
                                  .subject(user.getId().toString()) // Subject, typically user ID
                                  .groups(roles) // User roles/groups
                                  .expiresIn(Duration.ofHours(1))
                                  .sign(); // Sign with the private key configured
                return Optional.of(new LoginResponse(token, user.getId(), user.getEmail(), user.getRole().name()));
            }
        }
        LOG.infof("user NOT found");
        return Optional.empty();
    }
}
