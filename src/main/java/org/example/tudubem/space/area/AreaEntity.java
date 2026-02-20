package org.example.tudubem.space.area;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import org.example.tudubem.space.map.MapEntity;

@Data
@Entity
@Table(name = "area")
public class AreaEntity {

    // 영역 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상위 Map 참조
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "map_id", nullable = false)
    private MapEntity map;

    // 영역 이름
    @Column(nullable = false, length = 100)
    private String name;

    // 비즈니스 분류값 (예: DANGER, SAFE)
    @Column(length = 50)
    private String type;

    // 폴리곤 좌표 JSON (원점: 좌하단 0,0)
    @Lob
    @Column(name = "vertices_json", nullable = false)
    private String verticesJson;

    // 확장 메타데이터 JSON
    @Lob
    @Column(name = "metadata_json")
    private String metadataJson;
}
