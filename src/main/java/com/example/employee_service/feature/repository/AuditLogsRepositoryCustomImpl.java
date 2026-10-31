package com.example.employee_service.feature.repository;

import com.example.employee_service.feature.entity.AuditLogs;
import com.example.employee_service.feature.entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
@RequiredArgsConstructor
public class AuditLogsRepositoryCustomImpl implements AuditLogsRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<AuditLogs> search(String search, String module, String action, String result,
                                  LocalDateTime fromInclusive, LocalDateTime toExclusive,
                                  Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditLogs> query = criteriaBuilder.createQuery(AuditLogs.class);
        Root<AuditLogs> root = query.from(AuditLogs.class);
        query.select(root).where(predicates(criteriaBuilder, query, root, search, module, action,
                result, fromInclusive, toExclusive));

        List<Order> orders = new ArrayList<>();
        for (Sort.Order sortOrder : pageable.getSort()) {
            Path<?> field = root.get(sortOrder.getProperty());
            orders.add(sortOrder.isAscending()
                    ? criteriaBuilder.asc(field)
                    : criteriaBuilder.desc(field));
        }
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }

        TypedQuery<AuditLogs> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        typedQuery.setMaxResults(pageable.getPageSize());
        List<AuditLogs> content = typedQuery.getResultList();
        long total = count(criteriaBuilder, search, module, action, result, fromInclusive, toExclusive);
        return new PageImpl<>(content, pageable, total);
    }

    private long count(CriteriaBuilder criteriaBuilder, String search, String module, String action,
                       String result, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<AuditLogs> root = query.from(AuditLogs.class);
        query.select(criteriaBuilder.count(root)).where(predicates(criteriaBuilder, query, root, search,
                module, action, result, fromInclusive, toExclusive));
        return entityManager.createQuery(query).getSingleResult();
    }

    private Predicate[] predicates(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query,
                                   Root<AuditLogs> root, String search, String module, String action,
                                   String result, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        List<Predicate> predicates = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            Predicate logTextMatch = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("action"), "")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("entityType"), "")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("ipAddress"), "")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("requestUri"), "")), pattern));

            Subquery<Long> matchingUser = query.subquery(Long.class);
            Root<Users> user = matchingUser.from(Users.class);
            matchingUser.select(user.get("id"));
            matchingUser.where(
                    criteriaBuilder.equal(user.get("id"), root.get("user")),
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(user.get("userName"), "")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(user.get("fullName"), "")), pattern)));
            predicates.add(criteriaBuilder.or(logTextMatch, criteriaBuilder.exists(matchingUser)));
        }
        if (module != null && !module.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("entityType"), module));
        }
        if (action != null && !action.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("action"), action));
        }
        if (result != null && !result.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("result"), result));
        }
        if (fromInclusive != null) {
            Path<LocalDateTime> createdAt = root.get("createdAt");
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(createdAt, fromInclusive));
        }
        if (toExclusive != null) {
            Path<LocalDateTime> createdAt = root.get("createdAt");
            predicates.add(criteriaBuilder.lessThan(createdAt, toExclusive));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
