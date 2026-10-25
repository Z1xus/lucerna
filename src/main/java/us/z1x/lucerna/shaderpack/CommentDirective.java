package us.z1x.lucerna.shaderpack;

public class CommentDirective {
    public enum Type {
        DRAWBUFFERS,
        RENDERTARGETS
    }

    private final Type type;
    private final String directive;
    private final int location;

    CommentDirective(Type type, String directive, int location) {
        this.type = type;
        this.directive = directive;
        this.location = location;
    }

    public Type getType() {
        return type;
    }

    public String getDirective() {
        return directive;
    }

    // used to apply whichever of DRAWBUFFERS and RENDERTARGETS comes last
    public int getLocation() {
        return location;
    }
}
