package com.xkball.let_me_see_see.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xkball.let_me_see_see.LetMeSeeSee;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public class VanillaUtils {
    
    public static final Direction[] DIRECTIONS = Direction.values();
    public static final ResourceLocation MISSING_TEXTURE = ResourceLocation.withDefaultNamespace("missingno");
    public static final int TRANSPARENT = VanillaUtils.getColor(255, 255, 255, 0);
    public static final int GUI_GRAY = VanillaUtils.getColor(30, 30, 30, 200);
    
    public static ResourceLocation modRL(String path) {
        return rLOf(LetMeSeeSee.MODID, path);
    }
    
    public static ResourceLocation rLOf(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
    
    public static EquipmentSlot equipmentSlotFromHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }
    
    public static ItemInteractionResult itemInteractionFrom(InteractionResult result) {
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            case FAIL -> ItemInteractionResult.FAIL;
        };
    }
    
    public static void runCommand(String command, LivingEntity livingEntity) {
        // Raise permission level to 2, akin to what vanilla sign does
        CommandSourceStack cmdSrc = livingEntity.createCommandSourceStack().withPermission(2);
        var server = livingEntity.level().getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(cmdSrc, command);
        }
    }
    
    public static void runCommand(String command, MinecraftServer server, UUID playerUUID) {
        var player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(2), command);
        }
    }
    
    //irrelevant vanilla(笑)
    public static int getColor(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }
    
    public static int parseColorHEX(String color) throws IllegalArgumentException {
        if (color.length() == 6) {
            return getColor(
                    Integer.parseInt(color.substring(0, 2), 16),
                    Integer.parseInt(color.substring(2, 4), 16),
                    Integer.parseInt(color.substring(4, 6), 16),
                    255);
        }
        if (color.length() == 8) {
            return getColor(
                    Integer.parseInt(color.substring(0, 2), 16),
                    Integer.parseInt(color.substring(2, 4), 16),
                    Integer.parseInt(color.substring(4, 6), 16),
                    Integer.parseInt(color.substring(6, 8), 16)
            );
        }
        throw new IllegalArgumentException("Format of color must be RGB or RGBA digits");
    }
    
    public static String hexColorFromInt(int color) {
        var a = color >>> 24;
        var r = (color >> 16) & 0xFF;
        var g = (color >> 8) & 0xFF;
        var b = color & 0xFF;
        return String.format("%02X%02X%02X%02X", r, g, b, a).toUpperCase();
    }
    
    @SuppressWarnings("SuspiciousNameCombination")
    public static Vec2 rotate90FromBlockCenterYP(Vec2 point, int times) {
        times = times % 4;
        if (times == 0) return point;
        var x = point.x;
        var y = point.y;
        if (times == 1) return new Vec2(16 - y, x);
        if (times == 2) return new Vec2(16 - x, 16 - y);
        return new Vec2(y, 16 - x);
    }
    
    public static Component getName(Block block) {
        ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(block);
        return Component.translatable("block." + rl.getNamespace() + "." + rl.getPath());
    }
    
    public static String md5(String input) {
        try {
            var md = MessageDigest.getInstance("MD5");
            var bytes = md.digest(input.getBytes());
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String base64(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }
    
    public static String removeAfterLastCharOf(String str,char c){
        return str.substring(0,str.lastIndexOf(c));
    }
    
    public static List<String> searchStartWith(String key, Collection<String> src){
        var startWithList = new ArrayList<String>();
        for (var str : src) {
            var searchEntry = str.toLowerCase();
            if (searchEntry.startsWith(key)) startWithList.add(str);
        }
        startWithList.sort(String::compareTo);
        return startWithList;
    }
    
    public static List<String> searchInLowerCase(String key, Collection<String> src) {
        key = key.toLowerCase();
        var startWithList = new ArrayList<String>();
        var containsList = new ArrayList<String>();
        for (var str : src) {
            var searchEntry = str.toLowerCase();
            if (searchEntry.startsWith(key)) startWithList.add(str);
            else if (searchEntry.contains(key)) containsList.add(str);
        }
        startWithList.sort(String::compareTo);
        containsList.sort(String::compareTo);
        startWithList.addAll(containsList);
        return startWithList;
    }
    
    public static List<String> search(String key, Collection<String> src) {
        var startWithList = new ArrayList<String>();
        var containsList = new ArrayList<String>();
        for (var str : src) {
            if (str.startsWith(key)) startWithList.add(str);
            else if (str.contains(key)) containsList.add(str);
        }
        startWithList.sort(String::compareTo);
        containsList.sort(String::compareTo);
        startWithList.addAll(containsList);
        return startWithList;
    }
    
    public static class ClientHandler {
        
        @OnlyIn(Dist.CLIENT)
        public static void renderAxis(MultiBufferSource bufferSource, PoseStack poseStack) {
            var buffer = bufferSource.getBuffer(RenderType.debugLineStrip(8));
            var matrix = poseStack.last();
            buffer.addVertex(matrix, 0, 0, 0).setNormal(matrix, -1, 0, 0).setColor(0xFFFF0000);
            buffer.addVertex(matrix, 100, 0, 0).setNormal(matrix, 1, 0, 0).setColor(0xFFFF0000);
            buffer.addVertex(matrix, 0, 0, 0).setNormal(matrix, 0, -1, 0).setColor(0xFF00FF00);
            buffer.addVertex(matrix, 0, 100, 0).setNormal(matrix, 0, 1, 0).setColor(0xFF00FF00);
            buffer.addVertex(matrix, 0, 0, 0).setNormal(matrix, 0, 0, -1).setColor(0xFF0000FF);
            buffer.addVertex(matrix, 0, 0, 100).setNormal(matrix, 0, 0, 1).setColor(0xFF0000FF);
        }
        
    }
}
