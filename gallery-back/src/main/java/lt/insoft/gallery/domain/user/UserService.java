package lt.insoft.gallery.domain.user;

import lombok.RequiredArgsConstructor;
import lt.insoft.gallery.Image;
import lt.insoft.gallery.User;
import lt.insoft.gallery.domain.image.ImageRepository;
import org.springframework.data.jpa.domain.Specification;
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
    private final ImageRepository imageRepository;

    private Specification<User> findOneUser() {
        return  null;
    }

    @Transactional
    public boolean isAllowedUser(String uuid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (roles.contains("ROLE_admin")) return true;

        User user = userRepository.findByUsername(userDetails.getUsername());
        String realUuid = uuid;
        if (realUuid.endsWith("small")) {
            realUuid = realUuid.substring(0, realUuid.length()-5);
        }
        Image image = imageRepository.findByUuid(realUuid);
        if (image != null) {
            return image.getUser().getUsername().equals(user.getUsername());
        }
        return false;
    }
    @Transactional
    public boolean isAllowedUser(int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (roles.contains("ROLE_admin")) return true;

        User user = userRepository.findByUsername(userDetails.getUsername());
        Image image = imageRepository.findById(id);
        if (image != null) {
            return image.getUser().getUsername().equals(user.getUsername());
        }
        return false;
    }
}
