package com.gs.adapters.database;

import com.gs.adapters.repository.AssetRepository;
import com.gs.adapters.entity.Asset;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AssetDatabaseService {

    AssetRepository assetRepository;
    @Inject
    public AssetDatabaseService(AssetRepository assetRepository)
    {
        this.assetRepository = assetRepository;
    }
    public Uni<Asset> updateAsset(Asset updateAsset)
    {
        return assetRepository.persist(updateAsset);
    }
}
