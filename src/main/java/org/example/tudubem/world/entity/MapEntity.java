package org.example.tudubem.world.entity;

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
@Table(name = "map")
@Schema(description = "지도 정보")
public class MapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "지도 ID", example = "1")
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "지도 이름", example = "Hospital 1F")
    private String name;

    @Column(name = "sensor_map_image_path", length = 500)
    @Schema(description = "센서맵 이미지 파일 경로", example = "./data/map/abc-123.png")
    private String sensorMapImagePath;

}
