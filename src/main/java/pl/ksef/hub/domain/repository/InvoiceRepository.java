package pl.ksef.hub.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.ksef.hub.domain.entity.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Page<Invoice> findByTenantId(Long tenantId, Pageable pageable);
    
    Optional<Invoice> findByKsefNumber(String ksefNumber);
    
    List<Invoice> findByTenantIdAndStatus(Long tenantId, Invoice.InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId " +
           "AND i.invoiceDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:invoiceNumber IS NULL OR i.invoiceNumber LIKE %:invoiceNumber%)")
    Page<Invoice> searchInvoices(@Param("tenantId") Long tenantId,
                                  @Param("status") Invoice.InvoiceStatus status,
                                  @Param("invoiceNumber") String invoiceNumber,
                                  Pageable pageable);
    
    boolean existsByTenantIdAndInvoiceNumber(Long tenantId, String invoiceNumber);
}
