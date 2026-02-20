package org.example.tudubem.world;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class WorldService {

    private final WorldRepository worldRepository;

    public WorldService(WorldRepository worldRepository) {
        this.worldRepository = worldRepository;
    }

    public List<WorldEntity> findAll() {
        return worldRepository.findAll();
    }

    public Optional<WorldEntity> findById(Long id) {
        return worldRepository.findById(id);
    }

    @Transactional
    public WorldEntity create(WorldEntity worldEntity) {
        return worldRepository.save(worldEntity);
    }

    @Transactional
    public Optional<WorldEntity> update(Long id, WorldEntity request) {
        return worldRepository.findById(id)
                .map(worldEntity -> {
                    worldEntity.setName(request.getName());
                    return worldRepository.save(worldEntity);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!worldRepository.existsById(id)) {
            return false;
        }
        worldRepository.deleteById(id);
        return true;
    }
}
