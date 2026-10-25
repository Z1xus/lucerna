package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.basic.ASTNode;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;

// patches entity and shadow programs to read the instanced models argentum draws, see argentum's entity_instancing shaders
class InstancedEntityTransformer {
	private static final String[] TYPES = { "float", "vec2", "vec3", "vec4" };
	private static final String[] SWIZZLES = { ".x", ".xy", ".xyz", "" };
	// a value argentum never sets, so the branch keeps its uniforms alive without changing the output
	private static final String NEVER = "uAlphaPass == -2147483647";

	public static void transform(ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		switch (parameters.type) {
			case VERTEX:
				transformVertex(t, tree, root);
				break;
			case FRAGMENT:
				transformFragment(t, tree, root);
				break;
			default:
				throw new IllegalStateException("Instanced entities do not support " + parameters.type + " shaders");
		}
	}

	private static void transformVertex(ASTParser t, TranslationUnit tree, Root root) {
		boolean modern = tree.getVersionStatement().version.number >= 130;
		String in = modern ? "in" : "attribute";
		String out = modern ? "out" : "varying";

		replaceAttribute(t, tree, root, "mc_Entity", "vec4(-1.0, -1.0, 0.0, 1.0)");
		replaceAttribute(t, tree, root, "mc_midTexCoord", "vec4(0.0, 0.0, 0.0, 1.0)");
		replaceAttribute(t, tree, root, "at_tangent", "vec4(1.0, 0.0, 0.0, 1.0)");
		replaceAttribute(t, tree, root, "at_midBlock", "vec4(0.0)");
		removeUniform(t, root, "entityColor");

		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			in + " vec3 aPosition;",
			in + " vec2 aTexCoord;",
			in + " vec3 aNormal;",
			in + " vec4 aVertexColor;",
			in + " vec3 aModel0;",
			in + " vec3 aModel1;",
			in + " vec3 aModel2;",
			in + " vec3 aModel3;",
			in + " float aEffectTime;",
			in + " vec3 aLightCoord;",
			in + " vec4 aColor;",
			in + " vec4 aOverlay;",
			in + " vec4 aBoxTexture;",
			in + " vec3 aBoxSize;",
			"uniform int uBoxInstancing;",
			"uniform int uGlintPass;",
			"uniform int uChargePass;",
			"uniform int uItemGlintPass;",
			"uniform int uAlphaPass;",
			"uniform bool uEyeLight;",
			"uniform mat4 uItemGlintMatrix;",
			"uniform mat4 uItemGlintMatrix1;",
			"uniform vec3 uLightDirection0;",
			"uniform vec3 uLightDirection1;",
			out + " vec2 lucerna_texCoord;",
			out + " vec4 lucerna_color;",
			out + " vec4 lucerna_overlay;");
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
			"vec4 lucerna_vertex() { return mat4(vec4(aModel0, 0.0), vec4(aModel1, 0.0), vec4(aModel2, 0.0), vec4(aModel3, 1.0)) * vec4(aPosition, 1.0); }",
			"vec3 lucerna_normal() { return mat3(aModel0, aModel1, aModel2) * aNormal; }",
			// box instancing reuses the vertex colour slot for the uv layout that Box bakes
			"vec2 lucerna_texture() { vec2 size = max(aBoxTexture.zw, vec2(1.0)); return uBoxInstancing != 0 ? vec2((aBoxTexture.x + aVertexColor.x * aBoxSize.x + aVertexColor.y * aBoxSize.z) / size.x, (aBoxTexture.y + aVertexColor.z * aBoxSize.y + aVertexColor.w * aBoxSize.z) / size.y) : aTexCoord; }",
			"vec4 lucerna_vertexColor() { return aColor * (uBoxInstancing != 0 ? vec4(1.0) : aVertexColor); }",
			// 61680 is the light argentum uses for emissive parts
			"vec2 lucerna_light() { return uEyeLight ? vec2(61680.0, 0.0) : aLightCoord.xy; }",
			"vec4 lucerna_ftransform() { return gl_ModelViewProjectionMatrix * lucerna_vertex(); }");

		root.rename("ftransform", "lucerna_ftransform");
		root.replaceReferenceExpressions(t, "gl_Vertex", "lucerna_vertex()");
		root.replaceReferenceExpressions(t, "gl_Normal", "lucerna_normal()");
		root.replaceReferenceExpressions(t, "gl_MultiTexCoord0", "vec4(lucerna_texture(), 0.0, 1.0)");
		root.replaceReferenceExpressions(t, "gl_MultiTexCoord1", "vec4(lucerna_light(), 0.0, 1.0)");
		root.replaceReferenceExpressions(t, "gl_Color", "lucerna_vertexColor()");
		root.replaceReferenceExpressions(t, "entityColor", "aOverlay");

		tree.prependMain(t, "lucerna_texCoord = (gl_TextureMatrix[0] * vec4(lucerna_texture(), 0.0, 1.0)).xy;");
		tree.prependMain(t, "lucerna_color = lucerna_vertexColor();");
		tree.prependMain(t, "lucerna_overlay = aOverlay;");
		tree.appendMain(t, "if (" + NEVER + ") gl_Position.x += float(uBoxInstancing + uGlintPass + uChargePass + uItemGlintPass) + float(uEyeLight) + aEffectTime"
			+ " + uItemGlintMatrix[0][0] + uItemGlintMatrix1[0][0] + uLightDirection0.x + uLightDirection1.x;");
	}

	private static void transformFragment(ASTParser t, TranslationUnit tree, Root root) {
		boolean modern = tree.getVersionStatement().version.number >= 130;
		String in = modern ? "in" : "varying";

		removeUniform(t, root, "entityColor");
		root.rename("entityColor", "lucerna_overlay");

		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			"uniform sampler2D uTexture;",
			"uniform sampler2D uLightmap;",
			"uniform int uAlphaPass;",
			"uniform int uGlintPass;",
			"uniform int uItemGlintPass;",
			"uniform int uChargePass;",
			"uniform bool uLit;",
			"uniform bool uLightmapped;",
			"uniform vec4 u_FogColor;",
			"uniform float u_FogDensity;",
			"uniform float u_FogStart;",
			"uniform float u_FogEnd;",
			"uniform int u_FogShape;",
			in + " vec2 lucerna_texCoord;",
			in + " vec4 lucerna_color;",
			in + " vec4 lucerna_overlay;");

		String sample = modern ? "texture" : "texture2D";
		// the pack's own programs draw enchantment glint and charged layers, these argentum passes cannot be shaded
		tree.prependMain(t, "if (uGlintPass >= 0 || uItemGlintPass >= 0 || uChargePass != 0) discard;");
		// argentum splits translucent models into an opaque and a blended pass, see entity_instancing.frag
		tree.prependMain(t, "if (lucerna_alpha <= 0.1 || (uAlphaPass == 1 && lucerna_alpha >= 254.0 / 255.0) || (uAlphaPass == 2 && lucerna_alpha < 254.0 / 255.0)) discard;");
		tree.prependMain(t, "float lucerna_alpha = (" + sample + "(uTexture, lucerna_texCoord) * lucerna_color).a;");

		String output = root.identifierIndex.has("gl_FragData") ? "gl_FragData[0]" : "gl_FragColor";
		tree.appendMain(t, "if (" + NEVER + ") " + output + " += " + sample + "(uLightmap, vec2(0.0)) + u_FogColor"
			+ " + vec4(u_FogDensity + u_FogStart + u_FogEnd + float(u_FogShape) + float(uLit) + float(uLightmapped));");
	}

	private static void removeUniform(ASTParser t, Root root, String name) {
		for (String type : TYPES) {
			root.processMatches(t, new AutoHintedMatcher<ExternalDeclaration>("uniform " + type + " " + name + ";", Matcher.externalDeclarationPattern), ASTNode::detachAndDelete);
		}
	}

	private static void replaceAttribute(ASTParser t, TranslationUnit tree, Root root, String name, String value) {
		for (int i = 0; i < TYPES.length; i++) {
			boolean found = root.processMatches(t, new AutoHintedMatcher<ExternalDeclaration>("attribute " + TYPES[i] + " " + name + ";", Matcher.externalDeclarationPattern), ASTNode::detachAndDelete);
			found |= root.processMatches(t, new AutoHintedMatcher<ExternalDeclaration>("in " + TYPES[i] + " " + name + ";", Matcher.externalDeclarationPattern), ASTNode::detachAndDelete);

			if (found) {
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "const " + TYPES[i] + " " + name + " = " + value + SWIZZLES[i] + ";");
				return;
			}
		}
	}
}
