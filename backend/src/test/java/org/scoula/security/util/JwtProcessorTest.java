package org.scoula.security.util;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.config.RootConfig;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Log4j2
class JwtProcessorTest {

    @Autowired
    private JwtProcessor jwtProcessor;

    @Test
    void generateToken() {
        String token = jwtProcessor.generateToken("user0");
        assertNotNull(token);
    }

    @Test
    void getUsername_roundTrip() {
        String token = jwtProcessor.generateToken("user0");
        assertEquals("user0", jwtProcessor.getUsername(token));
    }

    @Test
    void validateToken_freshlyIssuedToken_isValid() {
        String token = jwtProcessor.generateToken("user0");
        assertTrue(jwtProcessor.validateToken(token));
    }

    @Test
    void generateRefreshToken_isMarkedAsRefreshToken() {
        String accessToken = jwtProcessor.generateToken("user0");
        String refreshToken = jwtProcessor.generateRefreshToken("user0");

        assertFalse(jwtProcessor.isRefreshToken(accessToken));
        assertTrue(jwtProcessor.isRefreshToken(refreshToken));
        assertEquals("user0", jwtProcessor.getUsername(refreshToken));
    }
}
