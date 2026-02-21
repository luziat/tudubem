package org.example.tudubem.world.keepout;

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
import org.example.tudubem.world.map.MapEntity;

@Data
@Entity
@Table(name = "keepout_zone")
@Schema(description = "고정 진입 금지 영역(Keepout Zone) 정보")
public class KeepoutZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "keepout ID", example = "100")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "map_id", nullable = false)
    @Schema(description = "상위 지도", hidden = true)
    private MapEntity map;

    @Column(nullable = false, length = 100)
    @Schema(description = "keepout 이름", example = "No-Go Zone A")
    private String name;

    @Lob
    @Column(name = "vertices_json", nullable = false)
    @Schema(description = "폴리곤 좌표 JSON (원점: 좌하단)", example = "[[0,0],[1000,0],[1000,300],[0,300]]")
    private String verticesJson;

    @Column(nullable = false)
    @Schema(description = "활성 여부", example = "true")
    private Boolean enabled = true;

    @Column(length = 300)
    @Schema(description = "설명/사유", example = "고정 장애물")
    private String reason;
}
