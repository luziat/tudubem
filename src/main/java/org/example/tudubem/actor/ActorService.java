package org.example.tudubem.actor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;

    public List<ActorEntity> findAll() {
        return actorRepository.findAll();
    }

    public Optional<ActorEntity> findById(Long id) {
        return actorRepository.findById(id);
    }

    @Transactional
    public ActorEntity create(ActorEntity actorEntity) {
        return actorRepository.save(actorEntity);
    }

    @Transactional
    public Optional<ActorEntity> update(Long id, ActorEntity request) {
        return actorRepository.findById(id)
                .map(actorEntity -> {
                    actorEntity.setName(request.getName());
                    return actorRepository.save(actorEntity);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!actorRepository.existsById(id)) {
            return false;
        }
        actorRepository.deleteById(id);
        return true;
    }
}
