package fit.iuh.models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.Set;
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore // Ngăn vòng lặp
    private Set<User> users;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Role(int id, Set<User> users, String name, String description) {
        this.id = id;
        this.users = users;
        this.name = name;
        this.description = description;
    }
    public Role(){

    };
}
