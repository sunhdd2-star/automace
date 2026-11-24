package com.example.automace;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoMaceClient implements ClientModInitializer {
    private static boolean enabled = true;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("automace").executes(ctx -> {
                enabled = !enabled;
                ctx.getSource().sendFeedback(Text.literal("Auto Mace: " + (enabled ? "ON" : "OFF")));
                return 1;
            }))
        );
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        if (!enabled) return;
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) return;
        if (client.currentScreen != null) return;

        if (player.isOnGround() || player.getVelocity().y > -0.5) return;

        if (!player.getMainHandStack().isOf(Items.MACE)) {
            for (int i = 0; i < 9; i++) {
                if (player.getInventory().getStack(i).isOf(Items.MACE)) {
                    player.getInventory().setSelectedSlot(i);
                    break;
                }
            }
            if (!player.getMainHandStack().isOf(Items.MACE)) return;
        }

        HitResult hit = client.crosshairTarget;
        if (hit instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof LivingEntity target
                && target.isAlive()
                && player.getAttackCooldownProgress(0.5f) >= 1.0f) {
            client.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
        }
    }
            }
