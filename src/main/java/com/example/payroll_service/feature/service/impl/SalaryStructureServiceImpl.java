package com.example.payroll_service.feature.service.impl;

import com.example.payroll_service.common.exception.BusinessException;
import com.example.payroll_service.common.exception.ResourceNotFoundException;
import com.example.payroll_service.common.response.DomainCode;
import com.example.payroll_service.feature.entity.SalaryStructure;
import com.example.payroll_service.feature.model.request.SalaryStructureRequest;
import com.example.payroll_service.feature.model.response.SalaryStructureResponse;
import com.example.payroll_service.feature.repository.SalaryStructureRepository;
import com.example.payroll_service.feature.service.SalaryStructureService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SalaryStructureServiceImpl implements SalaryStructureService {
    SalaryStructureRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<SalaryStructureResponse> getAll(String search) {
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return repository.findAllByOrderByEffectiveDateDescPositionCodeAsc().stream()
                .map(this::toResponse)
                .filter(item -> term.isEmpty()
                        || item.positionCode().toLowerCase(Locale.ROOT).contains(term)
                        || item.positionName().toLowerCase(Locale.ROOT).contains(term)
                        || item.grade().toLowerCase(Locale.ROOT).contains(term))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryStructureResponse getById(Long id) {
        return toResponse(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("cấu trúc lương " + id)));
    }

    @Override
    @Transactional
    public SalaryStructureResponse save(Long id, SalaryStructureRequest request) {
        String positionCode = request.positionCode().trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "-");
        repository.findByPositionCodeIgnoreCaseAndEffectiveDate(positionCode, request.effectiveDate())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException(DomainCode.CONFLICT, "cấu trúc lương " + positionCode + " tại ngày hiệu lực " + request.effectiveDate());
                });

        SalaryStructure structure = id == null
                ? new SalaryStructure()
                : repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("cấu trúc lương " + id));
        structure.setPositionCode(positionCode);
        structure.setBaseSalary(request.baseSalary());
        structure.setEffectiveDate(request.effectiveDate());
        structure.setAllowances(allowanceData(request));
        structure.setFormulaConfig(formulaData(request));
        return toResponse(repository.save(structure));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SalaryStructure structure = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("cấu trúc lương " + id));
        repository.delete(structure);
    }

    private JsonNode allowanceData(SalaryStructureRequest request) {
        // Giữ các trường phụ cấp của giao diện trong JSONB hiện có để không phải thêm cột cho từng biến thể cấu hình.
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put("total", request.allowanceTotal());
        data.put("summary", request.allowanceSummary() == null ? "" : request.allowanceSummary().trim());
        return data;
    }

    private JsonNode formulaData(SalaryStructureRequest request) {
        // Tên chức danh và mô tả công thức chưa có cột riêng trong schema nên được lưu cùng cấu hình công thức.
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put("positionName", request.positionName().trim());
        data.put("grade", request.grade().trim());
        data.put("status", request.status());
        data.put("formula", request.formula().trim());
        data.put("note", request.note() == null ? "" : request.note().trim());
        return data;
    }

    private SalaryStructureResponse toResponse(SalaryStructure structure) {
        JsonNode allowances = structure.getAllowances();
        JsonNode formulaConfig = structure.getFormulaConfig();
        return new SalaryStructureResponse(
                structure.getId(),
                structure.getPositionCode(),
                formulaConfig.path("positionName").asText(""),
                formulaConfig.path("grade").asText(""),
                structure.getBaseSalary(),
                allowances.path("total").decimalValue(),
                allowances.path("summary").asText(""),
                structure.getEffectiveDate(),
                formulaConfig.path("status").asText("Chờ áp dụng"),
                formulaConfig.path("formula").asText(""),
                formulaConfig.path("note").asText("")
        );
    }
}
