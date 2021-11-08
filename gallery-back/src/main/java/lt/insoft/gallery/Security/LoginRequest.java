package lt.insoft.gallery.Security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    private String username;
    private String password;
}
