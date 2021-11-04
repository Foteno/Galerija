package lt.insoft.gallery.gallery.Security;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SignupRequest {
    private String username;

    private String password;

    private Set<String> role;
}
