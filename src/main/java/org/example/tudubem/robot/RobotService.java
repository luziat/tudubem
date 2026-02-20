package org.example.tudubem.robot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RobotService {

    private final RobotRepository robotRepository;

    public List<RobotEntity> findAll() {
        return robotRepository.findAll();
    }

    public Optional<RobotEntity> findById(Long id) {
        return robotRepository.findById(id);
    }

    @Transactional
    public RobotEntity create(RobotEntity robotEntity) {
        return robotRepository.save(robotEntity);
    }

    @Transactional
    public Optional<RobotEntity> update(Long id, RobotEntity request) {
        return robotRepository.findById(id)
                .map(robotEntity -> {
                    robotEntity.setName(request.getName());
                    return robotRepository.save(robotEntity);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!robotRepository.existsById(id)) {
            return false;
        }
        robotRepository.deleteById(id);
        return true;
    }
}
