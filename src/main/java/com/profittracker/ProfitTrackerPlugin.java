package com.profittracker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;

import net.runelite.api.events.*;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;


import java.util.Arrays;
import java.util.stream.LongStream;

@Slf4j
@PluginDescriptor(
        name = "Profit Tracker"
)
public class ProfitTrackerPlugin extends Plugin
{
    ProfitTrackerGoldDrops GoldDropsObject;

    // the profit will be calculated against this value
    private long prevInventoryValue;
    private long currentProfit;

    private long startTickMillis;

    private boolean skipTickForProfitCalculation;
    private boolean inventoryValueChanged;


    @Inject
    private Client client;

    @Inject
    private ProfitTrackerConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ProfitTrackerOverlay overlay;

    @Override
    protected void startUp() throws Exception
    {
        // Add the inventory overlay
        overlayManager.add(overlay);

        ResetCalculations();

        GoldDropsObject = new ProfitTrackerGoldDrops(client, itemManager);


    }

    @Override
    protected void shutDown() throws Exception
    {
        // Remove the inventory overlay
        overlayManager.remove(overlay);

    }


    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        log.info("onItemContainerChanged container id: " + event.getContainerId());

        int containerId = event.getContainerId();

        if(containerId == InventoryID.INVENTORY.getId()) {
            inventoryValueChanged = true;

        }
//
        if(     containerId == InventoryID.BANK.getId() ||
                containerId == InventoryID.EQUIPMENT.getId()) {
            // this is a bank or equipment interaction.
            // Don't take this into
            skipTickForProfitCalculation = true;

        }

    }

    // calculate and update inventory value both in plugin and overlay
    private void updateInventoryValue()
    {
        long newInventoryValue;

        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
        if(container != null) {
            Item[] items = container.getItems();
            newInventoryValue = Arrays.stream(items).parallel().flatMapToLong(item ->
                LongStream.of(calculateItemValue(item))
            ).sum();
            // Update the panel
            overlay.updateInventoryValue(newInventoryValue);

            if (!skipTickForProfitCalculation)
            {
                updateProfit(newInventoryValue);

            }
            else
            {
                log.info("Skipping profit calculation!");
                // we skip profit calculation this time
                skipTickForProfitCalculation = false;
            }

            prevInventoryValue = newInventoryValue;

//
        }
    }

    // start profit calculation from this point
    private void ResetCalculations()
    {
        // value here doesn't matter
        prevInventoryValue = 0;

        currentProfit = 0;

        startTickMillis = System.currentTimeMillis();

        // skip profit calculation for first tick, to initialize first inventory value
        skipTickForProfitCalculation = true;
        inventoryValueChanged = false;
    }

    // calculate and update profit value both in plugin and overlay
    private void updateProfit(long inventoryValue)
    {
        long newProfit;

        if (prevInventoryValue == -1)
        {
            return;
        }
        // calculate new profit
        newProfit = inventoryValue - prevInventoryValue;

        // accumulate profit
        currentProfit += newProfit;

        overlay.updateProfitValue(currentProfit);

        // generate gold drop
        if (config.goldDrops() && newProfit != 0)
        {

            GoldDropsObject.requestGoldDrop(Math.toIntExact(newProfit));
        }

    }


    public long calculateItemValue(Item item) {
        int itemId = item.getId();
        log.info(String.format("calculateItemValue itemId = %d", itemId));

        if (itemId <= 0)
        {
            log.info("Bad item id!" + itemId);
            return 0;

        }

        ItemComposition itemComp = itemManager.getItemComposition(itemId);
        String itemName = itemComp.getName();
        int itemValue;
        // multiply quantity  GE value
        itemValue = item.getQuantity() * (itemManager.getItemPrice(item.getId()));

        return itemValue;
    }

    @Provides
    ProfitTrackerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ProfitTrackerConfig.class);
    }



    @Subscribe
    public void onScriptPreFired(ScriptPreFired scriptPreFired)
    {
        GoldDropsObject.onScriptPreFired(scriptPreFired);
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        long timeDeltaMillis;
        long currentTimeMillis;

        if (inventoryValueChanged)
        {
            updateInventoryValue();

            inventoryValueChanged = false;
        }


        // calculate time
        currentTimeMillis = System.currentTimeMillis();

        timeDeltaMillis = currentTimeMillis - startTickMillis;

        calculateProfitHourly(timeDeltaMillis);
    }

    void calculateProfitHourly(long timeDeltaMillis)
    {
        long averageProfitThousandForHour;
        long averageProfitForSecond;
        long secondsElapsed;

        secondsElapsed = timeDeltaMillis / 1000;
        if (secondsElapsed > 0)
        {
            averageProfitForSecond = (currentProfit) / secondsElapsed;
        }
        else
        {
            // can't divide by zero, not enough time has passed
            averageProfitForSecond = 0;
        }

        averageProfitThousandForHour = averageProfitForSecond * 3600 / 1000;

        overlay.updateProfitRate(averageProfitThousandForHour);

    }


    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        log.info(String.format("Click! ID: %d, actionParam: %d ,menuOption: %s, menuTarget: %s, widgetId: %d",
                event.getId(), event.getActionParam(), event.getMenuOption(), event.getMenuTarget(), event.getWidgetId()));

        if (event.getId() == ObjectID.BANK_DEPOSIT_BOX)
        {
            // we've interacted with a deposit box. Don't take this tick into account for profit calculation
            skipTickForProfitCalculation = true;
        }


    }
}
