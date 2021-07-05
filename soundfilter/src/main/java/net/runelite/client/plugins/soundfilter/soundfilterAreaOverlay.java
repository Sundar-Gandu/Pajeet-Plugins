package net.runelite.client.plugins.soundfilter;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
@Slf4j
public class soundfilterAreaOverlay extends OverlayPanel
{
    private final soundfilterPlugin plugin;
    private final soundfilterConfig config;
    private final Client client;

    @Inject
    public soundfilterAreaOverlay(soundfilterPlugin plugin, Client client, soundfilterConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setPosition(OverlayPosition.DETACHED);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setPreferredSize(new Dimension(10, 200));
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setPriority(OverlayPriority.LOW);
        this.plugin = plugin;
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics2D)
    {
        List<LayoutableRenderableEntity> renderableEntities = panelComponent.getChildren();
        renderableEntities.clear();
        renderableEntities.add(LineComponent.builder().left("Area Effects").build());

        List<Integer> gameSoundList = this.plugin.areaSoundsList;

        for (Integer sound : gameSoundList)
        {
            renderableEntities.add(
                    LineComponent.builder()
                            .left(sound + "")
                            .build());
        }

        return super.render(graphics2D);
    }
}
