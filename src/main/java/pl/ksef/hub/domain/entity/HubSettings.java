package pl.ksef.hub.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "hub_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "processing_enabled", nullable = false)
    private Boolean processingEnabled;
    
    @Column(name = "ksef_auto_connect", nullable = false)
    private Boolean ksefAutoConnect;
    
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
