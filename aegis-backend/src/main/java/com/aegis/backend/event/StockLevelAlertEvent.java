package com.aegis.backend.event;

import com.aegis.backend.entity.InventoryStatus;
import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StockLevelAlertEvent extends ApplicationEvent {
    private final UUID itemId;
    private final String sku;
    private final InventoryStatus previousStatus;
    private final InventoryStatus currentStatus;
    private final Integer quantity;

    public StockLevelAlertEvent(
            final Object source,
            final UUID itemId,
            final String sku,
            final InventoryStatus previousStatus,
            final InventoryStatus currentStatus,
            final Integer quantity) {
        super(source);
        this.itemId = itemId;
        this.sku = sku;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.quantity = quantity;
    }
}
