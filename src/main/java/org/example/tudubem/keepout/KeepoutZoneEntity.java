package org.example.tudubem.keepout;

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
import org.example.tudubem.world.WorldEntity;

@Data
@Entity
@Table(name = "keepout_zone")
public class KeepoutZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "world_id", nullable = false)
    private WorldEntity world;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name = "vertices_json", nullable = false)
    private String verticesJson;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 300)
    private String reason;
}
