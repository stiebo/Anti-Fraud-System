package antifraud.security;

import antifraud.domain.Role;
import antifraud.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleLoader {
    private final RoleRepository repository;

    @Autowired
    public RoleLoader(RoleRepository repository) {
        this.repository = repository;
        createRoles();
    }

    private void createRoles() throws RuntimeException {
        try {
            repository.save(new Role().setName("ADMINISTRATOR"));
        } catch (Exception e) {
        }
        try {
            repository.save(new Role().setName("MERCHANT"));
        } catch (Exception e) {
        }
        try {
            repository.save(new Role().setName("SUPPORT"));
        } catch (Exception e) {
        }
    }
}
