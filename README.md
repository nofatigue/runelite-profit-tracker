 <img src="https://oldschool.runescape.wiki/images/thumb/3/36/Coins_10000_detail.png/1024px-Coins_10000_detail.png?e07e3" width="200" title="hover text">
 
 
# Profit Tracker Plugin
NOTE: I've stopped working on this, I will probably not fix much of the issues. People are welcome to fork or use the code.


This runelite plugin tracks the profit you are generating, according to GE, while money-making.
![image](https://user-images.githubusercontent.com/8212109/94357201-5d4c1780-009f-11eb-9c73-17c279edd613.png)

For example, if you are filling vials, the plugin will accumlate profit each time you fill vial, accounting for empty vials price in GE.
Depositing or withdrawing items will not affect profit value.


# Gold drops
Every change in your inventory is monitored and a corresponding profit animation will be shown.
![image](https://user-images.githubusercontent.com/8212109/94357070-393c0680-009e-11eb-96a1-8fa7469ee6e1.png)

For example, if you buy in a general shop, an item for 20 coins, which is worth in GE 220 coins,
ProfitTracker will generate a gold drop animation of 200 coins.

# How to use
The plugin will simply begin tracking when it is loaded. So be sure to reload the plugin when you are starting your money routine!

# Running the plugin from repo
Clone the repo, and run ProfitTrackerTest java class from Intellij.

# Missing features
I've developed this while being F2P. 
There is no tracking of member stuff like tridents, dwarf cannon.

# Credits
Credit to wikiworm (Brandon Ripley) for his runelite plugin
https://github.com/wikiworm/InventoryValue
which helped for the creation of this plugin!

