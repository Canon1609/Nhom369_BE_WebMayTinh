package fit.iuh.DTO;

import fit.iuh.models.Role;

import java.util.Set;

public class UserDTO {
    private int id;
    private String username;
    private String email;
    private String fullname;
    private String phone;
    private Set<Role> roles;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserDTO(int id, String username, String email, String fullname, Set<Role> roles, String phone) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.roles  = roles;
        this.phone = phone;
    }
}
