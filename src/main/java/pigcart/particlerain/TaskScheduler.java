package pigcart.particlerain;

public class TaskScheduler {
    static Runnable task;
    static int ticksUntilRun;

    public static void scheduleDelayed(int tickDelay, Runnable runnable) {
        if (task == null) {
            task = runnable;
            ticksUntilRun = tickDelay;
        } else {
            ParticleRainClient.LOGGER.warn("Cancelled scheduling task because one is already scheduled");
        }
    }
    public static void tick() {
        if (task != null && --ticksUntilRun == 0) {
            task.run();
            task = null;
        }
    }
}
