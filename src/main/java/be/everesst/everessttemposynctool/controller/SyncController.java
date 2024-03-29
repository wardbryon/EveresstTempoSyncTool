package be.everesst.everessttemposynctool.controller;

import be.everesst.everessttemposynctool.service.LongRunningSyncInputService;
import be.everesst.everessttemposynctool.service.SyncInputTo;
import be.everesst.everessttemposynctool.service.SyncResultEntryTo;
import be.everesst.everessttemposynctool.service.SyncResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping(value = "/")
public class SyncController {

    private final LongRunningSyncInputService syncInputService;
    private final SyncResultService syncResultService;

    public SyncController(LongRunningSyncInputService syncInputService, SyncResultService syncResultService) {
        this.syncInputService = syncInputService;
        this.syncResultService = syncResultService;
    }

    @GetMapping(value = "/sync/{syncTableUUID}")
    public List<SyncResultEntryTo> findSyncEntitiesBySyncTableUUID(@PathVariable UUID syncTableUUID) {
        return syncResultService.findSyncResultEntry(syncTableUUID).stream().map(SyncResultEntryTo::map).toList();
    }

    @GetMapping(value = "/sync/{syncTableUUID}/slack")
    public ResponseEntity<Map<String, String>> findSlackInputBySyncTableUUID(@PathVariable UUID syncTableUUID) {
        String message = syncResultService.findSyncResultEntityByUUIDAsSlackInput(syncTableUUID);
        return ResponseEntity.ok(Collections.singletonMap("message", message));
    }

    @GetMapping(value = "/sync/{syncTableUUID}/{resourceId}/{date}")
    public List<SyncResultEntryTo> findSyncDayEntitiesBySyncTableUUIDResourceIdDate(@PathVariable UUID syncTableUUID, @PathVariable String resourceId, @PathVariable String date) {
        return syncResultService.findSyncResultEntry(syncTableUUID, resourceId).stream().map(SyncResultEntryTo::map)
                .filter(entryTo -> entryTo.startOfWeek().equals(LocalDate.parse(date)))
                .toList();
    }

    @GetMapping(value = "/sync/{syncTableUUID}/{resourceId}")
    public List<SyncResultEntryTo> findSyncDayEntitiesBySyncTableUUIDResourceId(@PathVariable UUID syncTableUUID, @PathVariable String resourceId) {
        return syncResultService.findSyncResultEntry(syncTableUUID, resourceId).stream().map(SyncResultEntryTo::map).toList();
    }

    @PostMapping(value = "/input")
    public void startSync(@RequestParam("syncUUID") String syncUUID, @RequestParam("syncFile") MultipartFile syncFile, @RequestParam("slackEmployeesFile") MultipartFile slackEmployeesFile, @RequestParam("clientId") String clientId, @RequestParam("clientSecret") String clientSecret) throws IOException {
        syncInputService.startCamisApi(new SyncInputTo(UUID.fromString(syncUUID), syncFile.getInputStream(), slackEmployeesFile.getInputStream(), clientId, clientSecret));
    }
}

