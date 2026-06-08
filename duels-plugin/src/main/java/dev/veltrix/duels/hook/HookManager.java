package dev.veltrix.duels.hook;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.hook.hooks.*;
import dev.veltrix.duels.hook.hooks.worldguard.WorldGuardHook;
import dev.veltrix.duels.util.hook.AbstractHookManager;

public class HookManager extends AbstractHookManager<DuelsPlugin> {

    public HookManager(final DuelsPlugin plugin) {
        super(plugin);
        register(BountyHuntersHook.NAME, BountyHuntersHook.class);
        register(CombatLogXHook.NAME, CombatLogXHook.class);
        register(CombatTagPlusHook.NAME, CombatTagPlusHook.class);
        register(DeluxeCombatHook.NAME, DeluxeCombatHook.class);
        register(EssentialsHook.NAME, EssentialsHook.class);
        register(FactionsHook.NAME, FactionsHook.class);
        register(LeaderHeadsHook.NAME, LeaderHeadsHook.class);
        register(McMMOHook.NAME, McMMOHook.class);
        register(MVdWPlaceholderHook.NAME, MVdWPlaceholderHook.class);
        register(PlaceholderHook.NAME, PlaceholderHook.class);
        register(MyPetHook.NAME, MyPetHook.class);
        register(PvPManagerHook.NAME, PvPManagerHook.class);
        register(SimpleClansHook.NAME, SimpleClansHook.class);
        register(VaultHook.NAME, VaultHook.class);
        register(WorldGuardHook.NAME, WorldGuardHook.class);
        register(AxGravesHook.NAME, AxGravesHook.class);
    }
}
