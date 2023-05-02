package be.everesst.everessttemposynctool.service;

import be.everesst.everessttemposynctool.model.sync.entities.SyncInputEntity;
import be.everesst.everessttemposynctool.model.sync.entities.SyncRecordEntity;
import com.cegeka.horizon.camis.sync_logger.model.SyncRecord;
import com.cegeka.horizon.camis.sync_timesheet.service.SyncTimesheetService;
import com.cegeka.horizon.camis.sync_timesheet.csv.HoursLoggedCsvReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.cegeka.horizon.camis.timesheet.Employee;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static com.cegeka.horizon.camis.web_client_factory.WebClientFactory.getWebClient;

@Service
public class SyncInputService {

    SyncRecordService syncRecordService;

    SyncResultService syncResultService;

    SyncTimesheetService syncTimesheetService;

    public SyncInputService(SyncTimesheetService syncTimesheetService, SyncResultService syncResultService, SyncRecordService syncRecordService){
        this.syncTimesheetService = syncTimesheetService;
        this.syncResultService = syncResultService;
        this.syncRecordService = syncRecordService;
    }
    public void startCamisApi(SyncInputEntity syncInputEntity) throws IOException {
        WebClient webClient = getWebClient("https://gw.api.cegeka.com/1/erp/camis/v1/prd/", "68d8012c-f2c7-4a9d-a24b-d9b8f718f4a2", "uC1pT8wN7mN5fX6xF5uU3rQ8vY8dX8nE5eW1tI4kL8gE8mO6vJ");
        List<Employee> employees = new HoursLoggedCsvReader(new FileInputStream("/home/thomas/git/Camis/csv_import_Camis_API.csv")).readCsv();
        List<SyncRecord> syncRecords = syncTimesheetService.sync(webClient, employees);
        syncRecordEntitiesSaveToSyncResultEntity(syncRecordsToSyncRecordEntities(syncRecords), syncInputEntity.getSyncResultUUID());
    }

   private List<SyncRecordEntity> syncRecordsToSyncRecordEntities(List<SyncRecord> syncRecords) {
        List<SyncRecordEntity> syncRecordEntities = new ArrayList<>();
        for (int index = 0; index < syncRecords.size(); index++) {
            SyncRecord currentSyncRecord = syncRecords.get(index);
            syncRecordEntities.add(new SyncRecordEntity(index,
                    currentSyncRecord.getMessage(),
                    currentSyncRecord.getEmployeeName(),
                    currentSyncRecord.getStartDate(),
                    currentSyncRecord.getHoursLogged(),
                    currentSyncRecord.getWorkOrder()));
        }
        return syncRecordEntities;
    }

    private void syncRecordEntitiesSaveToSyncResultEntity(List<SyncRecordEntity> syncRecordEntities, UUID syncResultUUID) {
        syncResultService.createSyncResult(syncResultUUID);
        for (SyncRecordEntity syncRecordEntity : syncRecordEntities) {
            syncRecordService.saveRecord(syncResultUUID, syncRecordEntity);
        }
    }
}
