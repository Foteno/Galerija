package lt.insoft.gallery.gallery.domain.user;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {
    @GetMapping("login")
    public String login() {
        return "a";
    }

    @GetMapping("get")
    @Secured("user")
    public String getUsername() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return securityContext.getAuthentication().getName();
    }

    @Secured({"user", "admin"})
    public boolean isValidUsername(String username) {
        return true;
    }
}
