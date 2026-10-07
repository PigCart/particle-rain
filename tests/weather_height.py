"""Run with Python 3 and a JDK on PATH; no game/Gradle required.

Compiles the actual 1.20.1 calculateHeight method against a small
position-dependent collision fixture. This is a contract regression,
not an in-game rendering integration test.
"""
from pathlib import Path
import subprocess
import tempfile

source = (Path(__file__).resolve().parents[1] /
          "src/main/java/pigcart/particlerain/ParticleSpawner.java").read_text()
start = source.index("    public static int calculateHeight(")
end = source.index("\n    }", start) + len("\n    }")
method = source[start:end]

fixture = """
public class HeightRegression {
    static class BlockPos {
        int x, y, z;
        BlockPos(int x, int y, int z) { this.x=x; this.y=y; this.z=z; }
        static class MutableBlockPos extends BlockPos {
            MutableBlockPos(int x, int y, int z) { super(x,y,z); }
            void setY(int y) { this.y=y; }
        }
    }
    static class Heightmap { enum Types { MOTION_BLOCKING } }
    record Shape(boolean isEmpty) {}
    static class BlockState {
        int x, y, z;
        BlockState(BlockPos pos) { x=pos.x; y=pos.y; z=pos.z; }
        Shape getCollisionShape(ClientLevel level, BlockPos pos) {
            // The block is solid only at its own position on the support layer.
            return new Shape(pos.x != x || pos.y != y || pos.z != z || y != 8);
        }
        Shape getFluidState() { return new Shape(true); }
    }
    static class ClientLevel {
        int getHeight(Heightmap.Types type, int x, int z) { return 10; }
        BlockState getBlockState(BlockPos pos) { return new BlockState(pos); }
    }
    static boolean isIgnored(BlockState state) { return false; }
    static BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(999,64,999);
    public static void main(String[] args) {
        for (int x : new int[]{-31,0,27})
            for (int z : new int[]{-15,0,83})
                if (calculateHeight(new ClientLevel(), x, z) != 9)
                    throw new AssertionError("wrong collision position at "+x+","+z);
        System.out.println("9 position-dependent height checks passed");
    }
    METHOD
}
"""

with tempfile.TemporaryDirectory() as directory:
    path = Path(directory) / "HeightRegression.java"
    # Require the unfixed method to fail the same contract test.
    for label, implementation in [
        ("original", method.replace("getCollisionShape(level, mutablePos)",
                                    "getCollisionShape(level, pos)")),
        ("fixed", method),
    ]:
        path.write_text(fixture.replace("METHOD", implementation), encoding="utf-8")
        subprocess.run(["javac", str(path)], check=True)
        result = subprocess.run(["java", "-cp", directory, "HeightRegression"],
                                capture_output=True, text=True)
        assert (result.returncode == 0) == (label == "fixed"), result.stderr
        print(label + ": " + (result.stdout.strip() or "expected assertion failure"))
