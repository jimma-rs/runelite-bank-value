package spudjb.bankvalue;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Bank Cache",
	description = "Caches the bank"
)
@Slf4j
public class BankCachePlugin extends Plugin
{
	@Inject
	Client client;

	@Inject
	ClientToolbar clientToolbar;

	@Inject
	ItemManager itemManager;

	private BankCachePanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		panel = new BankCachePanel(this);

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(BankCachePlugin.class, "panel_icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Bank Cache")
			.priority(5)
			.panel(panel)
			.icon(icon)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getItemContainer() != client.getItemContainer(InventoryID.BANK))
		{
			return;
		}

		final List<CachedItem> cachedItems = new ArrayList<>(event.getItemContainer().getItems().length);
		for (Item item : event.getItemContainer().getItems())
		{
			int itemPrice = itemManager.getItemPrice(item.getId());
			ItemComposition itemDefinition = client.getItemDefinition(item.getId());
			
			cachedItems.add(new CachedItem(item.getId(), item.getQuantity(), itemDefinition.getName(), itemPrice));
		}


		SwingUtilities.invokeLater(() -> panel.populate(cachedItems));
	}
}
