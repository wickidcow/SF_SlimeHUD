package io.github.schntgaispock.slimehud.waila;

import io.github.schntgaispock.slimehud.SlimeHUD;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class SchedulerAdapter {

    private static final boolean FOLIA = detectFolia();

    private SchedulerAdapter() {}

    public static TaskHandle runPlayerRepeating(Player player, long periodTicks, Runnable action) {
        long period = Math.max(1L, periodTicks);

        if (FOLIA) {
            ScheduledTask task = player.getScheduler().runAtFixedRate(
                    SlimeHUD.getInstance(), ignored -> action.run(), null, 1L, period);
            return new TaskHandle() {
                @Override
                public void cancel() {
                    task.cancel();
                }

                @Override
                public boolean isCancelled() {
                    return task.isCancelled();
                }
            };
        }

        BukkitTask task = SlimeHUD.getInstance().getServer().getScheduler().runTaskTimer(
                SlimeHUD.getInstance(), action, 1L, period);
        return new TaskHandle() {
            @Override
            public void cancel() {
                task.cancel();
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }
        };
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public interface TaskHandle {
        void cancel();
        boolean isCancelled();
    }
}
