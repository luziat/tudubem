package org.example.tudubem.actor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Actor 실시간 상태")
public record ActorStatus(
        @Schema(description = "Actor ID", example = "1")
        Long actorId,
        @Schema(description = "x 좌표", example = "12.5")
        double x,
        @Schema(description = "y 좌표", example = "8.0")
        double y,
        @Schema(description = "말풍선 텍스트", example = "장애물 발견")
        String speech
) {
}
