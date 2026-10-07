"""Python 3 + JDK; pass the path to a JOML 1.10.5 jar.

Compiles the exact helpers from CustomParticle, without a game runtime.
"""
from pathlib import Path
import os
import subprocess
import sys
import tempfile

source = (Path(__file__).resolve().parents[1] /
          "src/main/java/pigcart/particlerain/particle/CustomParticle.java").read_text()
helpers = source[source.index("    private static float velocityAngle"):
                 source.index("    public void renderCameraCopyQuad")]
fixture = """
import org.joml.*;
import java.util.Random;
public class VelocityRegression {
    HELPERS
    static int assertions;
    static void check(boolean value) {
        assertions++;
        if (!value) throw new AssertionError(assertions);
    }
    static Quaternionf original(Vector3f v) {
        return new Quaternionf(new AxisAngle4f(
            -org.joml.Math.acos(new Vector3f(v).normalize().y),
            new Vector3f(-v.z,0,v.x).normalize()));
    }
    static Quaternionf fixed(Vector3f v) {
        return new Quaternionf(new AxisAngle4f(-velocityAngle(v), velocityAxis(v)));
    }
    public static void main(String[] args) {
        for (Vector3f v : new Vector3f[]{new Vector3f(), new Vector3f(0,1,0), new Vector3f(0,-1,0)}) {
            check(!original(v).isFinite());
            check(fixed(v).isFinite());
        }
        Random random = new Random(12345);
        for (int i=0; i<100000; i++) {
            Vector3f v = new Vector3f(random.nextFloat()*2-1,random.nextFloat()*2-1,random.nextFloat()*2-1);
            check(fixed(v).isFinite());
            check(new Vector3f(1,1,0).rotate(original(v)).distance(
                  new Vector3f(1,1,0).rotate(fixed(v))) < 1e-6f);
        }
        System.out.println(assertions+" numerical assertions passed");
    }
}
"""
# Avoid the java.lang.Math/org.joml.Math wildcard ambiguity.
fixture = fixture.replace("import org.joml.*;", "import org.joml.*; import org.joml.Math;")
jar = str(Path(sys.argv[1]).resolve())
with tempfile.TemporaryDirectory() as directory:
    path = Path(directory) / "VelocityRegression.java"
    path.write_text(fixture.replace("HELPERS", helpers), encoding="utf-8")
    subprocess.run(["javac", "-cp", jar, str(path)], check=True)
    subprocess.run(["java", "-cp", directory + os.pathsep + jar, "VelocityRegression"], check=True)
