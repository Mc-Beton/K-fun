package pl.ksef.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ksef.hub.domain.entity.HubSettings;
import pl.ksef.hub.domain.repository.HubSettingsRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubControlService {
    
    private final HubSettingsRepository hubSettingsRepository;
    private final SystemNotificationService notificationService;
    
    @Transactional
    public void startProcessing() {
        log.info("Starting hub processing");
        HubSettings settings = getOrCreateSettings();
        settings.setProcessingEnabled(true);
        settings.setUpdatedBy("SYSTEM");
        hubSettingsRepository.save(settings);
        
        notificationService.notifyHubInfo(
            "Przetwarzanie uruchomione",
            "Hub został włączony i rozpoczął przetwarzanie komunikatów."
        );
    }
    
    @Transactional
    public void stopProcessing() {
        log.info("Stopping hub processing");
        HubSettings settings = getOrCreateSettings();
        settings.setProcessingEnabled(false);
        settings.setUpdatedBy("SYSTEM");
        hubSettingsRepository.save(settings);
        
        notificationService.notifyHubInfo(
            "Przetwarzanie zatrzymane",
            "Hub został zatrzymany i nie przetwarza nowych komunikatów."
        );
    }
    
    @Transactional
    public void enableKsefAutoConnect() {
        log.info("Enabling KSeF auto-connect");
        HubSettings settings = getOrCreateSettings();
        settings.setKsefAutoConnect(true);
        settings.setUpdatedBy("SYSTEM");
        hubSettingsRepository.save(settings);
        
        notificationService.notifyHubInfo(
            "Automatyczne łączenie z KSeF włączone",
            "Hub będzie automatycznie próbował łączyć się z serwerem KSeF."
        );
    }
    
    @Transactional
    public void disableKsefAutoConnect() {
        log.info("Disabling KSeF auto-connect");
        HubSettings settings = getOrCreateSettings();
        settings.setKsefAutoConnect(false);
        settings.setUpdatedBy("SYSTEM");
        hubSettingsRepository.save(settings);
        
        notificationService.notifyHubInfo(
            "Automatyczne łączenie z KSeF wyłączone",
            "Hub nie będzie automatycznie łączył się z serwerem KSeF."
        );
    }
    
    public HubSettings getSettings() {
        return getOrCreateSettings();
    }
    
    public boolean isProcessingEnabled() {
        return getSettings().getProcessingEnabled();
    }
    
    public boolean isKsefAutoConnectEnabled() {
        return getSettings().getKsefAutoConnect();
    }
    
    private HubSettings getOrCreateSettings() {
        return hubSettingsRepository.findFirstByOrderByIdDesc()
                .orElseGet(() -> {
                    HubSettings defaultSettings = HubSettings.builder()
                            .processingEnabled(true)
                            .ksefAutoConnect(true)
                            .updatedBy("SYSTEM")
                            .build();
                    return hubSettingsRepository.save(defaultSettings);
                });
    }
}
