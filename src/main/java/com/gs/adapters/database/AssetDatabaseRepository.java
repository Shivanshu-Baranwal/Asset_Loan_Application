package com.gs.adapters.database;

import com.gs.adapters.repository.AssetRepository;
import com.gs.adapters.entity.Asset;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class AssetDatabaseRepository {

    AssetRepository assetRepository;

    @Inject
    public AssetDatabaseRepository(AssetRepository assetRepository)
    {
        this.assetRepository = assetRepository;
    }

    public Uni<Asset> findAsset(UUID id, Boolean flag) {
        return assetRepository.find("id=?1 and occupied=?2", id, flag)
                .firstResult()
                .onItem()
                .ifNull()
                .failWith(()->  {
                    throw new RuntimeException("Either Asset already occupied or Asset doesn't exist");
                });
    }

    public Uni<Asset> findAssetById(UUID id) {
        return assetRepository.find("id=?1", id)
                .firstResult()
                .onItem()
                .ifNull()
                .failWith(()->new RuntimeException("Asset Id not found"));
    }

}
