package pigcart.particlerain;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import java.util.ArrayList;

public class TaskScheduler {
    private static int time;
    public static final ArrayList<ObjectIntImmutablePair<Runnable>> tasks = new ArrayList<>();

    public static void scheduleDelayed(int tickDelay, Runnable runnable) {
        tasks.add(ObjectIntImmutablePair.of(runnable, time + tickDelay));
    }
    public static void tick() {
        if (tasks.isEmpty()) return;
        time++;
        // reverse for loop avoids concurrency issues
        for (int i = tasks.size() - 1; i >= 0; i--) {
            ObjectIntImmutablePair<Runnable> task = tasks.get(i);
            if (task.rightInt() < time) {
                task.left().run();
                tasks.remove(task);
            }
        }
    }
}
