package us.z1x.lucerna.gl.program;

import us.z1x.lucerna.gl.GlResource;
import us.z1x.lucerna.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL20;

public final class Program extends GlResource {
    private final ProgramUniforms uniforms;
    private final ProgramSamplers samplers;
    private final ProgramImages images;

    Program(int program, ProgramUniforms uniforms, ProgramSamplers samplers, ProgramImages images) {
        super(program);

        this.uniforms = uniforms;
        this.samplers = samplers;
        this.images = images;
    }

    public void use() {
        IrisRenderSystem.useProgram(getGlId());

        uniforms.update();
        samplers.update();
        images.update();
    }

    public static void unbind() {
        ProgramUniforms.clearActiveUniforms();
        ProgramSamplers.clearActiveSamplers();
        IrisRenderSystem.useProgram(0);
    }

    public void destroyInternal() {
        GL20.glDeleteProgram(getGlId());
    }

    /**
     * @return the OpenGL ID of this program.
     * @deprecated this should be encapsulated eventually
     */
    @Deprecated
    public int getProgramId() {
        return getGlId();
    }

    public int getActiveImages() {
        return images.getActiveImages();
    }
}
