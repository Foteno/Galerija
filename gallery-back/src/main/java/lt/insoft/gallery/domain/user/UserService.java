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

    private Specification<User> findOneUser() { // FIXME: nereikalingas
        return  null;
    }

    @Transactional
    public boolean isAllowedUser(String uuid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication(); // FIXME: IDE man čia rodo, kad dublikuotas kodas. reiktų kažką išsikelti
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (roles.contains("ROLE_admin")) return true;

        User user = userRepository.findByUsername(userDetails.getUsername());
        String realUuid = uuid;
        if (realUuid.endsWith("small")) {
            realUuid = realUuid.substring(0, realUuid.length()-5); // FIXME: šita logika yra labai specifiškai susijusi su image, ne su user. Tai ir turėtų būti imageService
        }
        Image image = imageRepository.findByUuid(realUuid);
        if (image != null) {
            return image.getUser().getUsername().equals(user.getUsername());
        }
        return false;
    }
    @Transactional
    public boolean isAllowedUser(int id) { // FIXME: koks čia id? kodo viduje tas matosi, bet metodo signature jau turėtų tą pasakyti.
                                           //  paprastai, jei pvz. userService turi metodą findById(int id), tas id reiškia user'io id. kitu atveju reiktų atitinkamai užvadinti
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (roles.contains("ROLE_admin")) return true; // FIXME: {}

        User user = userRepository.findByUsername(userDetails.getUsername());
        Image image = imageRepository.findById(id); // FIXME: Kiek kartų image selectinamas iš DB trynimo metu?
        if (image != null) {
            return image.getUser().getUsername().equals(user.getUsername());
        }
        return false;
    }
}
