package fit.iuh.services;

import fit.iuh.models.Role;
import fit.iuh.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService  {
    private RoleRepository Rolerepository;

    public Optional<Role> findbyName(String name){
        return Rolerepository.findByName(name);
    }
}
