package pigcart.particlerain.particle.render;

//? if >=1.21.9 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;

import static net.minecraft.client.renderer.RenderPipelines.PARTICLE_SNIPPET;

public class BlendedParticleRenderType {

    public static final RenderPipeline FOG_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(PARTICLE_SNIPPET)
                    .withLocation("pipeline/particlerain_fog")
                    //? >=26.1 {
                    /^.withColorTargetState(new com.mojang.blaze3d.pipeline.ColorTargetState(
                            BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(new com.mojang.blaze3d.pipeline.DepthStencilState(
                            com.mojang.blaze3d.platform.CompareOp.GREATER_THAN, false))
                    ^///?} else {
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthWrite(false)
                    //?}
                    .build()
    );
    public static final SingleQuadParticle.Layer INSTANCE =
            new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, FOG_PIPELINE);
}

*///?} else if 1.21.1 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jetbrains.annotations.Nullable;

public class BlendedParticleRenderType {
    public static final ParticleRenderType INSTANCE = new ParticleRenderType() {
        @Override
        public @Nullable BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            //RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            //RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            //RenderSystem.setShaderTexture(2, VersionUtil.getMcId("dynamic/light_map_1"));
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }
    };
}
*///?} else if 1.20.1 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public class BlendedParticleRenderType {
    public static final ParticleRenderType INSTANCE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.depthMask(false); // controls whether particles are blended together or masked out
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }
        @Override
        public void end(Tesselator tessellator) {
            tessellator.end();
        }
    };
}
//?}