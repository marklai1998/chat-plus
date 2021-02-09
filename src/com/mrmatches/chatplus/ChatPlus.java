package com.mrmatches.chatplus;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ChatPlus extends JavaPlugin implements Listener {
    private FileConfiguration invite_config;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Plugin invite = Bukkit.getPluginManager().getPlugin("InviteWhiteList");
        if (invite != null) invite_config = Bukkit.getPluginManager().getPlugin("InviteWhiteList").getConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        String format = event.getFormat();
        format = format.replace("%1$s", event.getPlayer().getDisplayName()).replace("%2$s", "");
        TextComponent name = new TextComponent(format);

        if (invite_config != null) {
            invite_config = Bukkit.getPluginManager().getPlugin("InviteWhiteList").getConfig();
            String invited_by = invite_config.getString("Name." + event.getPlayer().getDisplayName() + ".invite_by");
            if (!invited_by.equalsIgnoreCase("@") && !invited_by.equalsIgnoreCase("CONSOLE")) {
                invited_by = invite_config.getString("UUIDs." + event.getPlayer().getUniqueId().toString());
            }
            BaseComponent[] hoverEventComponents = new BaseComponent[]{
                    new TextComponent(ChatColor.GREEN + "邀請人： " + ChatColor.RESET + invited_by)
            };
            HoverEvent name_hover_event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverEventComponents);
            name.setHoverEvent(name_hover_event);
        }

        TextComponent final_message = new TextComponent("");
        final_message.addExtra(name);
        String message = event.getMessage().replaceAll("%", "%%");
        String[] message_parts = message.split(" ");
        for (String part : message_parts) {
            Player sender = event.getPlayer();
            if (part.length() > 1) {
                if (part.matches("^#(.*)")) {
                    part = ChatColor.BLUE + part;
                } else if (part.matches("^@(.*)")) {
                    Player receiver = Bukkit.getServer().getPlayer(part.substring(1));
                    if (receiver == null) {
                        part = ChatColor.GRAY + part;
                    } else {
                        part = ChatColor.GREEN + "@" + receiver.getDisplayName();
                        receiver.playNote(receiver.getLocation(), Instrument.XYLOPHONE, new Note(10));
                        receiver.sendTitle("", ChatColor.GREEN + sender.getDisplayName() + "在一則留言中提及了你", 10, 30, 10);
                        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            public void run() {
                                receiver.playNote(receiver.getLocation(), Instrument.XYLOPHONE, new Note(24));
                            }
                        }, 3L);
                    }
                } else if (part.equals("[全身]") || part.equals("[右手]") || part.equals("[頭盔]") || part.equals("[胸甲]") || part.equals("[護腿]") || part.equals("[靴子]") || part.equals("[左手]") || part.equals("[座標]")) {
                    TextComponent component = new TextComponent();
                    switch (part) {
                        case "[全身]":
                            TextComponent main_hand = send_item(sender.getEquipment().getItemInMainHand(), true);
                            if (main_hand != null && !main_hand.getText().equals("")) {
                                component.addExtra(main_hand);
                                component.addExtra(" ");
                            }
                            TextComponent off_hand = send_item(sender.getEquipment().getItemInOffHand(), true);
                            if (off_hand != null && !off_hand.getText().equals("")) {
                                component.addExtra(off_hand);
                                component.addExtra(" ");
                            }
                            TextComponent helmet = send_item(sender.getEquipment().getHelmet(), false);
                            if (helmet != null && !helmet.getText().equals("")) {
                                component.addExtra(helmet);
                                component.addExtra(" ");
                            }
                            TextComponent chestplate = send_item(sender.getEquipment().getChestplate(), false);
                            if (chestplate != null && !chestplate.getText().equals("")) {
                                component.addExtra(chestplate);
                                component.addExtra(" ");
                            }
                            TextComponent leggings = send_item(sender.getEquipment().getLeggings(), false);
                            if (leggings != null && !leggings.getText().equals("")) {
                                component.addExtra(leggings);
                                component.addExtra(" ");
                            }
                            TextComponent boots = send_item(sender.getEquipment().getBoots(), false);
                            if (boots != null && !boots.getText().equals("")) {
                                component.addExtra(boots);
                                component.addExtra(" ");
                            }
                            break;
                        case "[右手]":
                            component = send_item(sender.getEquipment().getItemInMainHand(), true);
                            break;
                        case "[左手]":
                            component = send_item(sender.getEquipment().getItemInOffHand(), true);
                            break;
                        case "[頭盔]":
                            component = send_item(sender.getEquipment().getHelmet(), false);
                            break;
                        case "[胸甲]":
                            component = send_item(sender.getEquipment().getChestplate(), false);
                            break;
                        case "[護腿]":
                            component = send_item(sender.getEquipment().getItemInMainHand(), true);
                            break;
                        case "[靴子]":
                            component = send_item(sender.getEquipment().getBoots(), false);
                            break;
                        case "[座標]":
                            component.setText("[" + ChatColor.GRAY + "X:" + (int) event.getPlayer().getLocation().getX() + ", Y:" + (int) event.getPlayer().getLocation().getY() + ", Z:" + (int) event.getPlayer().getLocation().getZ() + ChatColor.RESET + "]");
                    }
                    if (component != null) {
                        final_message.addExtra(component);
                        final_message.addExtra(" " + ChatColor.RESET);
                    }
                    continue;
                }
            }
            final_message.addExtra(part + " " + ChatColor.RESET);
        }
        if (final_message.getExtra() != null) {
            for (Player recipients : event.getRecipients()) {
                recipients.spigot().sendMessage(final_message);
            }
        }
    }

    private TextComponent send_item(ItemStack item, boolean count) {
        if (item != null && item.getType() != Material.AIR) {
            TextComponent component = new TextComponent();
            String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().replaceAll("_", " ");
            name = org.apache.commons.lang.StringUtils.capitalize(name.toLowerCase());

            // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
            Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
            Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

            // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
            Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
            Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
            Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

            Object nmsNbtTagCompoundObj; // This will just be an empty NBTTagCompound instance to invoke the saveNms method
            Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
            Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method

            try {
                nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
                nmsItemStackObj = asNMSCopyMethod.invoke(null, item);
                itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
            } catch (Throwable t) {
                Bukkit.getLogger().log(Level.SEVERE, "failed to serialize itemstack to nms item", t);
                return null;
            }
            String json = itemAsJsonObject.toString();
            BaseComponent[] hoverEventComponents = new BaseComponent[]{
                    new TextComponent(json)
            };
            HoverEvent hover_event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
            if (count || item.getAmount() > 1) {
                component.setText("[" + ChatColor.GRAY + name + ChatColor.RESET + " x " + ChatColor.YELLOW + item.getAmount() + ChatColor.RESET + "]");
            } else {
                component.setText("[" + ChatColor.GRAY + name + ChatColor.RESET + "]");
            }
            component.setHoverEvent(hover_event);
            return component;
        } else return null;
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String[] buffer_parts = event.getBuffer().split(" ");
        Bukkit.getLogger().log(Level.SEVERE,"dasd");
        Bukkit.getLogger().log(Level.SEVERE,buffer_parts.toString());
        String last_buffer = buffer_parts[buffer_parts.length - 1];
        if (last_buffer.matches("^@(.*)")) {
            event.setCompletions(make_player_list(last_buffer.substring(1), event.getSender().getName()));
        } else if (last_buffer.matches("^\\[(.*)")) {
            event.setCompletions(make_item_list(last_buffer.substring(1)));
        }
    }

    private List<String> make_player_list(String input, String sender_name) {
        List<String> list = new ArrayList<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            if (!player.getDisplayName().equalsIgnoreCase(sender_name) && player.getDisplayName().matches("(?i:" + input + ".*)"))
                list.add("@" + player.getDisplayName());
        }
        return list;
    }

    private List<String> make_item_list(String input) {
        List<String> list = new ArrayList<>();
        String[] names = {"[座標]", "[全身]", "[右手]", "[左手]", "[頭盔]", "[胸甲]", "[護腿]", "[靴子]"};
        for (String name : names) {
            if (name.matches("^" + input + "(.*)")) list.add(name);
        }
        return list;
    }
}
