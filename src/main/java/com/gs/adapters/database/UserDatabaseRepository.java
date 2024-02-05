package com.gs.adapters.database;

import com.gs.adapters.repository.UserRepository;
import com.gs.adapters.entity.Users;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UserDatabaseRepository {

    UserRepository userRepository;

    @Inject
    public UserDatabaseRepository(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }
    public Uni<Users> findUserByRole(UUID id, String role) {
        return userRepository.find("id=?1 and role=?2", id, role)
                .firstResult()
                .onItem()
                .ifNull()
                .failWith(() -> new RuntimeException(role + " not found"));
    }

    public Uni<Users> findUser(UUID id)
    {
        return userRepository.find("id=?1", id)
                .firstResult()
                .onItem()
                .ifNull()
                .failWith(()-> new RuntimeException("Invalid User"));
    }
}
