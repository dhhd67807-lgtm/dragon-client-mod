package com.dragonclient.core;

import com.dragonclient.DragonClientMod;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class CommandManager {
    
    public CommandManager() {
        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Register .dragon command
            dispatcher.register(ClientCommandManager.literal("dragon")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("§6Dragon Client v" + DragonClientMod.VERSION));
                    context.getSource().sendFeedback(Text.literal("§7Use §e.dragon help §7for commands"));
                    return 1;
                })
                .then(ClientCommandManager.literal("help")
                    .executes(context -> {
                        context.getSource().sendFeedback(Text.literal("§6=== Dragon Client Commands ==="));
                        context.getSource().sendFeedback(Text.literal("§e.dragon §7- Show version"));
                        context.getSource().sendFeedback(Text.literal("§e.dragon toggle <module> §7- Toggle module"));
                        context.getSource().sendFeedback(Text.literal("§e.dragon list §7- List all modules"));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("toggle")
                    .then(ClientCommandManager.argument("module", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(context -> {
                            String moduleName = com.mojang.brigadier.context.StringRange.between(0, 0).get(context.getInput());
                            DragonClientMod.getInstance().getModuleManager().toggleModule(moduleName);
                            context.getSource().sendFeedback(Text.literal("§aToggled module: " + moduleName));
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("list")
                    .executes(context -> {
                        context.getSource().sendFeedback(Text.literal("§6=== Dragon Client Modules ==="));
                        DragonClientMod.getInstance().getModuleManager().getModules().forEach(module -> {
                            String status = module.isEnabled() ? "§a✓" : "§c✗";
                            context.getSource().sendFeedback(Text.literal(status + " §7" + module.getName()));
                        });
                        return 1;
                    })
                )
            );
        });
    }
}
