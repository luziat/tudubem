package org.example.tudubem.robot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "robot")
@Schema(description = "로봇 정보")
public class RobotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "로봇 ID", example = "1")
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "로봇 이름", example = "AMR-01")
    private String name;
}
