package co.tz.sheriaconnectapi.services.AuthServices;

import co.tz.sheriaconnectapi.abstractions.Command;
import co.tz.sheriaconnectapi.exceptions.UserNotFoundException;
import co.tz.sheriaconnectapi.model.DTOs.LogoutInput;
import co.tz.sheriaconnectapi.model.Entities.User;
import co.tz.sheriaconnectapi.repositories.RefreshTokenRepository;
import co.tz.sheriaconnectapi.repositories.UserRepository;
import co.tz.sheriaconnectapi.security.Jwt.ClientType;
import co.tz.sheriaconnectapi.utils.ResponseUtil;
import co.tz.sheriaconnectapi.utils.StandardResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService implements Command<LogoutInput, Void> {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    @Override
    public ResponseEntity<StandardResponse<Void>> execute(
            LogoutInput input
    ) {

        if (input.getAuth() == null || !input.getAuth().isAuthenticated()) {
            return ResponseUtil.error(
                    "Unauthorized",
                    HttpStatus.UNAUTHORIZED
            );
        }

        // 1️⃣ Resolve user
        User user = userRepository
                .findByEmail(input.getAuth().getName())
                .orElseThrow(UserNotFoundException::new);

        // 2️⃣ Delete all refresh tokens (logout everywhere)
        refreshTokenRepository.deleteAllByUserId(user.getId());

        // 3️⃣ Detect client type (default WEB)
        String clientHeader =
                input.getRequest().getHeader("X-Client-Type");

        ClientType clientType = ClientType.WEB;

        if (clientHeader != null) {
            try {
                clientType = ClientType.valueOf(
                        clientHeader.toUpperCase()
                );
            } catch (IllegalArgumentException ignored) {}
        }

        // 4️⃣ WEB → clear HttpOnly refresh cookie
        if (clientType == ClientType.WEB) {
            Cookie cookie = new Cookie("refresh_token", "");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/auth/refresh");
            cookie.setMaxAge(0); // delete cookie
            input.getResponse().addCookie(cookie);
        }

        // 5️⃣ Clear Spring Security context
        SecurityContextHolder.clearContext();

        return ResponseUtil.success(
                null,
                "Logout successful",
                HttpStatus.OK
        );
    }
}
