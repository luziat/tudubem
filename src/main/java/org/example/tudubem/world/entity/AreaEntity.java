package org.example.tudubem.world.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "area")
@Schema(description = "지도 영역(Area) 정보")
public class AreaEntity {

    // 영역 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "영역 ID", example = "10")
    private Long id;

    // 상위 Map 참조
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "map_id", nullable = false)
    @Schema(description = "상위 지도", hidden = true)
    private MapEntity map;

    // 영역 이름
    @Column(nullable = false, length = 100)
    @Schema(description = "영역 이름", example = "ER Zone")
    private String name;

    // 비즈니스 분류값 (예: DANGER, SAFE)
    @Column(length = 50)
    @Schema(description = "영역 분류", example = "DANGER")
    private String type;

    // 폴리곤 좌표 JSON (원점: 좌하단 0,0)
    @Lob
    @Column(name = "vertices_json", nullable = false)
    @Schema(description = "폴리곤 좌표 JSON (원점: 좌하단)", example = "[[0,0],[120,0],[120,80],[0,80]]")
    private String verticesJson;

    // 확장 메타데이터 JSON
    @Lob
    @Column(name = "metadata_json")
    @Schema(description = "영역 메타데이터 JSON", example = "{\"riskLevel\":\"HIGH\"}")
    private String metadataJson;
}
