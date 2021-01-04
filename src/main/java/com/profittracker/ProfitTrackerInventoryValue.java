package com.profittracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;

import java.util.Arrays;
import java.util.stream.LongStream;

@Slf4j
public class ProfitTrackerInventoryValue {
    /*
    Provide functional methods for calculating inventory value
     */
    /*
    Singletons which will be provided at creation by the plugin
     */

    static final int EMPTY_SLOT_ITEMID = -1;

    private final ItemManager itemManager;
    private final Client client;

    public ProfitTrackerInventoryValue( Client client, ItemManager itemManager) {
        this.client = client;
        this.itemManager = itemManager;
    }

    private long calculateItemValue(Item item) {
        /*
        Calculate GE value of single item
         */

        int itemId = item.getId();

        if (itemId < -1)
        {
            // unexpected
            log.info("Bad item id!" + itemId);
            return 0;

        }

        if (itemId == EMPTY_SLOT_ITEMID)
        {
            return 0;
        }

        log.info(String.format("calculateItemValue itemId = %d", itemId));

        // multiply quantity  by GE value
        return item.getQuantity() * (itemManager.getItemPrice(itemId));
    }

    public long calculateContainerValue(InventoryID ContainerID)
    {
        /*
        calculate total inventory value
         */

        long newInventoryValue;

        ItemContainer container = client.getItemContainer(ContainerID);

        if (container == null)
        {
            return 0;
        }

        Item[] items = container.getItems();

        newInventoryValue = Arrays.stream(items).parallel().flatMapToLong(item ->
                LongStream.of(calculateItemValue(item))
        ).sum();

        return newInventoryValue;
    }


    public long calculateInventoryValue()
    {
        /*
        calculate total inventory value
         */

        return calculateContainerValue(InventoryID.INVENTORY);

    }

    public long calculateEquipmentValue()
    {
        /*
        calculate total equipment value
         */
        return calculateContainerValue(InventoryID.EQUIPMENT);
    }

    public long calculateInventoryAndEquipmentValue()
    {
        /*
        calculate total inventory + equipment value
         */

        return calculateInventoryValue() + calculateEquipmentValue();
    }


}
