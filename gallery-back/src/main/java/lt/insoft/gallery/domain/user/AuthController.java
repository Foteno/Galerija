package lt.insoft.gallery.domain.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import lt.insoft.gallery.Security.JwtResponse;
import lt.insoft.gallery.Security.JwtUtils;
import lt.insoft.gallery.Security.LoginRequest;
import lt.insoft.gallery.Security.SignupRequest;
import lt.insoft.gallery.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@CommonsLog
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Validated LoginRequest loginRequest) {
        log.info(loginRequest.getUsername());
        log.info(loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        for (String s: roles) {
            System.out.println(s); // FIXME. Kažkur ~10 eilučių aukščiau naudoji log.info, o čia System.out. Neišlaikai consistency netgi vieno metoto viduje
        }
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Validated SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Username taken");
        }
        User user = new User(signupRequest.getUsername(), String.valueOf(LocalTime.now()),
                encoder.encode(signupRequest.getPassword()));

        Set<String> role = signupRequest.getRole();
        if (role != null) {
            // FIXME: gauni set'ą, bet su juo elgiesi kaip su tiesiog String'u. Ar tikrai reikalingas set'as.
            //  Ir ar teko matyti saitą, kur registruojantis pasirenki, ar būsi adminas? :)
            user.setRole(role.iterator().next());
        } else {
            user.setRole("user");
        }

        userRepository.save(user);
        return ResponseEntity.ok("Registration success");
    }

}
