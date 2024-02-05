package com.gs.adapters.repository;

import com.gs.adapters.entity.Asset;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AssetRepository implements PanacheRepository<Asset> {
}
