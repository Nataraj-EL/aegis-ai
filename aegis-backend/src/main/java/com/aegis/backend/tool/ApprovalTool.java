package com.aegis.backend.tool;

import com.aegis.backend.entity.ApprovalRequest;
import com.aegis.backend.entity.ApprovalStatus;
import com.aegis.backend.repository.ApprovalRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ApprovalTool implements Tool {

    private final ApprovalRepository approvalRepository;

    public ApprovalTool(final ApprovalRepository approvalRepository) {
        this.approvalRepository = approvalRepository;
    }

    @Override
    public String getId() {
        return "approval_summary";
    }

    @Override
    public String getName() {
        return "Approval Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Retrieves workflow items currently waiting for approval. Accepts an optional 'approver' parameter to filter by user.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        final List<ApprovalRequest> requests;
        if (arguments != null && arguments.get("approver") != null) {
            final String approver = (String) arguments.get("approver");
            requests = approvalRepository.findByApproverAndStatus(approver, ApprovalStatus.PENDING);
        } else {
            requests = approvalRepository.findByStatus(ApprovalStatus.PENDING);
        }

        final List<Map<String, Object>> list = new ArrayList<>();
        for (final ApprovalRequest request : requests) {
            final Map<String, Object> item = new HashMap<>();
            item.put("id", request.getId().toString());
            item.put("entityType", request.getEntityType().name());
            item.put("entityId", request.getEntityId().toString());
            item.put("requester", request.getRequester());
            item.put("approver", request.getApprover());
            item.put("status", request.getStatus().name());
            item.put("comments", request.getComments());
            item.put(
                    "createdAt",
                    request.getCreatedAt() != null ? request.getCreatedAt().toString() : "");
            list.add(item);
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("pendingCount", requests.size());
        result.put("pendingRequests", list);
        return result;
    }
}
