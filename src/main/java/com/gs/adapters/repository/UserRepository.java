package com.gs.adapters.repository;

import com.gs.adapters.entity.Users;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<Users> {
}
