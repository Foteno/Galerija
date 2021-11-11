package lt.insoft.gallery.domain.user;

import lombok.RequiredArgsConstructor;
import lt.insoft.gallery.Image;
import lt.insoft.gallery.User;
import lt.insoft.gallery.domain.image.IImageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final IImageService imageService;

    @Transactional
    public boolean isNotAllowedUser(String uuid, UserDetailsImpl userDetails) {
        User user = getUserFromLoggedIn(userDetails);
        if (user == null) return false;
        Image image = imageService.getImageByThumbnailUuid(uuid);
        if (image != null) {
            return !image.getUser().getUsername().equals(user.getUsername());
        }
        return true;
    }

    public User getUserFromLoggedIn(UserDetailsImpl userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (roles.contains("ROLE_admin")) {
            return null;
        }

        return userRepository.findByUsername(userDetails.getUsername());
    }
}
