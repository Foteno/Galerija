package lt.insoft.gallery.Security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private int id;
    private String username;
    private List<String> roles;

    public JwtResponse(String token, int id, String username, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }
}
