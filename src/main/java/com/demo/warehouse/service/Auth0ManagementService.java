package com.demo.warehouse.service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Response;
import com.auth0.net.TokenRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Auth0ManagementService {

    @Value("${auth0.issuer}")
    private String issuer;

    @Value("${auth0.management.clientId}")
    private String clientId;

    @Value("${auth0.management.clientSecret}")
    private String clientSecret;

    @Value("${auth0.management.connection}")
    private String connection;

    private final EmailService emailService;

    public String createUser(String email, String password) throws Exception {
        String domain = issuer.replace("https://", "").replace("/", "");
        AuthAPI auth = AuthAPI.newBuilder(domain, clientId, clientSecret).build();

        // Get token for Management API
        TokenRequest tokenRequest = auth.requestToken(issuer + "api/v2/");
        TokenHolder holder = tokenRequest.execute().getBody();

        ManagementAPI mgmt = ManagementAPI.newBuilder(domain, holder.getAccessToken()).build();

        User user = new User(connection);
        user.setEmail(email);
        user.setPassword(password.toCharArray());
        user.setEmailVerified(true);

        Response<User> response = mgmt.users().create(user).execute();
        String auth0Sub = response.getBody().getId();
        
        // Send notification email
        emailService.sendNewUserNotification(email, auth0Sub);
        
        return auth0Sub; // Returns the Auth0 Sub (auth0|...)
    }

    public void deleteUser(String auth0Sub) {
        if (auth0Sub == null || auth0Sub.isEmpty()) return;
        
        try {
            String domain = issuer.replace("https://", "").replace("/", "");
            AuthAPI auth = AuthAPI.newBuilder(domain, clientId, clientSecret).build();
            TokenRequest tokenRequest = auth.requestToken(issuer + "api/v2/");
            TokenHolder holder = tokenRequest.execute().getBody();
            
            ManagementAPI mgmt = ManagementAPI.newBuilder(domain, holder.getAccessToken()).build();
            mgmt.users().delete(auth0Sub).execute();
        } catch (Exception e) {
            log.error("Error deleting user from Auth0: {}", auth0Sub, e);
        }
    }
}
