
package com.profittracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ProfitTrackerTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(ProfitTrackerPlugin.class);
        RuneLite.main(args);
    }
}
