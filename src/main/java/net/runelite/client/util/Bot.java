package net.runelite.client.util;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.flexo.Flexo;
import net.runelite.client.game.ItemManager;

import java.awt.*;
import java.util.List;
import java.util.*;

@Slf4j
public class Bot extends Flexo {
    private TabUtils tabUtils;
    private ItemManager itemManager;

    public Bot(Client client, TabUtils tabUtils, ItemManager itemManager) throws AWTException {
        super();
        Flexo.client = client;
        this.tabUtils = tabUtils;
        this.itemManager = itemManager;
    }

    public Player test() {
        return client.getLocalPlayer();
    }

    public ArrayList<GameObject> getGameObjectsById(int... objects) {
        ArrayList<Integer> objectIDs = new ArrayList<>();
        for (int i : objects) {
            objectIDs.add(i);
        }

        Scene scene = client.getScene();
        Tile[][] tiles = scene.getTiles()[client.getPlane()];
        ArrayList<GameObject> found = new ArrayList<>();

        for (Tile[] tiles2 : tiles) {
            for (Tile tile : tiles2) {
                for (GameObject object : tile.getGameObjects()) {
                    if (object == null) {
                        continue;
                    }

                    if (objectIDs.contains(object.getId())) {
                        found.add(object);
                        continue;
                    }

                    ObjectDefinition comp = client.getObjectDefinition(object.getId());
                    ObjectDefinition impostor = comp.getImpostorIds() != null ? comp.getImpostor() : comp;

                    if (impostor != null && objectIDs.contains(impostor.getId())) {
                        found.add(object);
                    }
                }
            }
        }

        return found;
    }

    public List<WidgetItem> getInventoryItems(int... itemIds) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        ArrayList<Integer> itemIDs = new ArrayList<>();
        List<WidgetItem> list = new ArrayList<>();

        for (int i : itemIds) {
            itemIDs.add(i);
        }

        for (WidgetItem i : inventoryWidget.getWidgetItems()) {
            if (itemIDs.contains(i.getId())) {
                list.add(i);
            }

        }

        return list;
    }

    public WidgetItem getInventoryItem(int itemID) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        WidgetItem item = null;

        for (WidgetItem i : inventoryWidget.getWidgetItems()) {
            if (i.getId() == itemID) {
                item = i;
                break;
            }
        }

        return item;
    }

    public void clickInventoryItems(List<WidgetItem> list) {
        if (list.isEmpty()) {
            return;
        }

        for (WidgetItem item : list) {
            clickInventoryItem(item);
        }
    }

    public void clickInventoryItem(WidgetItem item) {
        if (client.getWidget(WidgetInfo.INVENTORY).isHidden()) {
            this.keyPress(tabUtils.getTabHotkey(Tab.INVENTORY));
        }

        if (item != null) {
            String name = Integer.toString(item.getId());
            if (itemManager.getItemDefinition(item.getId()) != null) {
                name = itemManager.getItemDefinition((item.getId())).getName();

            }

            log.debug("Grabbing getCanvasBounds of " + name);

            if (item.getCanvasBounds() != null) {
                clickWidgetItem(item);
            } else {
                log.debug("Could not find getCanvasBounds of " + name);
            }
        }
    }

    public void clickWidgetItem(WidgetItem item) {
        clickPoint(getWidgetItemClickPoint(item));
    }

    public void clickGameObject(GameObject obj) {
        clickPoint(getGameObjectClickPoint(obj));
    }

    public void clickWidget(Widget wid) {
        clickPoint(getWidgetClickPoint(wid));
    }

    public void clickActor(Actor act) {
        clickPoint(getActorClickPoint(act));
    }

    public Point getWidgetItemClickPoint(WidgetItem item) {
        return getRectClickPoint(item.getCanvasBounds());
    }

    public Point getGameObjectClickPoint(GameObject obj) {
        return getPolyClickPoint(obj.getConvexHull());
    }

    public Point getWidgetClickPoint(Widget wid) {
        return getRectClickPoint(wid.getBounds());
    }

    public Point getActorClickPoint(Actor act) {
        return getPolyClickPoint(act.getConvexHull());
    }

    public Point getRectClickPoint(Rectangle rect) {
        int rand = (Math.random() <= 0.5) ? 1 : 2;
        int x = (int) (rect.getX() + (rand * 3) + rect.getWidth() / 2);
        int y = (int) (rect.getY() + (rand * 3) + rect.getHeight() / 2);
        return new Point(x, y);
    }

    public Point getPolyClickPoint(Polygon poly) {
        Rectangle rect = poly.getBounds();
        Random rand = new Random();
        int x, y;

        do {
            x = ((int) rect.getX()) + rand.nextInt((int) rect.getWidth());
            y = ((int) rect.getY()) + rand.nextInt((int) rect.getHeight());
        } while (!poly.contains(x, y));

        return new Point(x, y);
    }

    public boolean clickPoint(Point cp) {
        if (cp.getX() >= 1 && cp.getY() >= 1) {
            log.debug("Attempting mouse click @ " + cp.getX() + ", " + cp.getY());

            this.mouseMove(cp.getX(), cp.getY());
            this.mousePressAndRelease(1);

            return true;
        } else {
            log.debug("Invalid mouse click supplied @ " + cp.getX() + ", " + cp.getY());
            return false;
        }
    }

    public int[] getIDsFromString(String str) {
        str.replace(" ", "");
        return Arrays.stream(str.split(","))
                .map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    public boolean isInventoryFull() {
        return client.getWidget(WidgetInfo.INVENTORY).getWidgetItems().size() == 28;
    }

    public int rand(int low, int high) {
        return (int) (Math.random() * (high - low)) + low;
    }

    public void sortNpcByDistance(List<NPC> npcs) {
        final LocalPoint cameraPoint = new LocalPoint(client.getCameraX(), client.getCameraY());
        npcs.sort(Comparator.comparing(npc -> -1 * npc.getLocalLocation().distanceTo(cameraPoint)));
    }
}