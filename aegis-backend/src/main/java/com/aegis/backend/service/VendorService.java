package com.aegis.backend.service;

import com.aegis.backend.dto.VendorCreateRequest;
import com.aegis.backend.dto.VendorResponse;
import com.aegis.backend.entity.Vendor;
import com.aegis.backend.entity.VendorStatus;
import com.aegis.backend.repository.ProcurementRepository;
import com.aegis.backend.repository.VendorRepository;
import com.aegis.backend.repository.VendorSpecifications;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final ProcurementRepository procurementRepository;

    public VendorService(final VendorRepository vendorRepository, final ProcurementRepository procurementRepository) {
        this.vendorRepository = vendorRepository;
        this.procurementRepository = procurementRepository;
    }

    @Transactional
    public VendorResponse createVendor(final VendorCreateRequest request) {
        if (vendorRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format("Vendor with name '%s' already exists", request.getName()));
        }

        final Vendor vendor = Vendor.builder()
                .name(request.getName())
                .contactEmail(request.getContactEmail())
                .category(request.getCategory())
                .status(request.getStatus())
                .rating(request.getRating())
                .build();

        final Vendor saved = vendorRepository.save(vendor);
        return mapToResponse(saved);
    }

    @Transactional
    public VendorResponse updateVendor(final UUID id, final VendorCreateRequest request) {
        final Vendor vendor =
                vendorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vendor not found"));

        vendorRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException(
                        String.format("Vendor with name '%s' already exists", request.getName()));
            }
        });

        vendor.setName(request.getName());
        vendor.setContactEmail(request.getContactEmail());
        vendor.setCategory(request.getCategory());
        vendor.setStatus(request.getStatus());
        vendor.setRating(request.getRating());

        final Vendor saved = vendorRepository.save(vendor);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteVendor(final UUID id) {
        final Vendor vendor =
                vendorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vendor not found"));

        if (procurementRepository.existsByVendorId(id)) {
            throw new IllegalStateException("Cannot delete vendor. It is referenced by active procurement requests.");
        }

        vendor.setStatus(VendorStatus.INACTIVE);
        vendorRepository.save(vendor);
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> getVendors(
            final VendorStatus status, final String category, final BigDecimal minRating) {
        Specification<Vendor> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(VendorSpecifications.withStatus(status));
        }
        if (category != null) {
            spec = spec.and(VendorSpecifications.withCategory(category));
        }
        if (minRating != null) {
            spec = spec.and(VendorSpecifications.ratingGreaterThanOrEqualTo(minRating));
        }

        return vendorRepository.findAll(spec).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendor(final UUID id) {
        final Vendor vendor =
                vendorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vendor not found"));
        return mapToResponse(vendor);
    }

    private VendorResponse mapToResponse(final Vendor vendor) {
        return VendorResponse.builder()
                .id(vendor.getId())
                .name(vendor.getName())
                .contactEmail(vendor.getContactEmail())
                .category(vendor.getCategory())
                .status(vendor.getStatus())
                .rating(vendor.getRating())
                .createdAt(vendor.getCreatedAt())
                .build();
    }
}
