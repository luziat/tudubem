package org.example.tudubem.monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("monitor")
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private static final long DEFAULT_ACTOR_ID = 1L;
    private final MonitorService monitorService;

    @GetMapping(value = "/{mapId}/trajectory-image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> trajectoryImage(
            @PathVariable Long mapId,
            @RequestParam(required = false) Long actorId
    ) {
        long targetActorId = actorId == null ? DEFAULT_ACTOR_ID : actorId;
        return monitorService.getTrajectoryImage(mapId, targetActorId);
    }
}
