package org.example.tudubem.actor;

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
@Table(name = "actor")
@Schema(description = "동적 객체(Actor) 정보")
public class ActorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Actor ID", example = "1")
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "Actor 식별자", example = "forklift-01")
    private String name;
}
