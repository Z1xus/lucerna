package net.coderbot.iris.uniforms.custom;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import kroppeb.stareval.element.ExpressionElement;
import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.expression.VariableExpression;
import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import kroppeb.stareval.parser.Parser;
import kroppeb.stareval.resolver.ExpressionResolver;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.parsing.IrisFunctions;
import net.coderbot.iris.parsing.IrisOptions;
import net.coderbot.iris.parsing.VectorType;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.HardcodedCustomUniforms;
import net.coderbot.iris.uniforms.custom.cached.CachedUniform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class CustomUniforms implements FunctionContext {
	private final Map<String, CachedUniform> variables = new Object2ObjectLinkedOpenHashMap<>();
	private final Map<String, Expression> variablesExpressions = new Object2ObjectLinkedOpenHashMap<>();
	private final CustomUniformFixedInputUniformsHolder inputHolder;
	private final Set<CachedUniform> uniforms = new ObjectOpenHashSet<>();
	private final Set<CachedUniform> assigned = new ObjectOpenHashSet<>();
	private final List<CachedUniform> orderedUniforms;
	private final List<CachedUniform> uniformOrder = new ArrayList<>();
	private final Map<CachedUniform, List<CachedUniform>> dependsOn;
	private final Map<CachedUniform, List<CachedUniform>> requiredBy;
	private boolean optimised;

	private CustomUniforms(CustomUniformFixedInputUniformsHolder inputHolder, CustomUniformFixedInputUniformsHolder hardcodedHolder, Map<String, Builder.Variable> variables) {
		this.inputHolder = inputHolder;
		ExpressionResolver resolver = new ExpressionResolver(
			IrisFunctions.functions,
			(name) -> {
				Type type = this.inputHolder.getType(name);
				if (type != null)
					return type;
				Builder.Variable variable = variables.get(name);
				if (variable != null)
					return variable.type;
				return null;
			},
			true);

		for (Builder.Variable variable : variables.values()) {
			try {
				Expression expression = resolver.resolveExpression(variable.type, variable.expression);
				CachedUniform cachedUniform = CachedUniform
					.forExpression(variable.name, variable.type, expression, this);
				this.addVariable(expression, cachedUniform);
				if (variable.uniform) {
					this.uniforms.add(cachedUniform);
				}
			} catch (Exception e) {
				Iris.logger
					.warn("Failed to resolve uniform " + variable.name + ", reason: " + e
						.getMessage() + " ( = " + variable.expression + ")", e);
			}
		}

		{
			// toposort

			this.dependsOn = new Object2ObjectOpenHashMap<>();
			this.requiredBy = new Object2ObjectOpenHashMap<>();
			Object2IntMap<CachedUniform> dependsOnCount = new Object2IntOpenHashMap<>();

			for (CachedUniform input : this.inputHolder.getAll()) {
				requiredBy.put(input, new ObjectArrayList<>());
			}

			for (CachedUniform input : this.variables.values()) {
				requiredBy.put(input, new ObjectArrayList<>());
			}

			// hardcoded uniforms are only provided when the pack does not define one with the same name
			for (CachedUniform hardcoded : hardcodedHolder.getAll()) {
				if (!this.hasVariable(hardcoded.getName())) {
					requiredBy.put(hardcoded, new ObjectArrayList<>());
					this.uniforms.add(hardcoded);
				}
			}

			FunctionReturn functionReturn = new FunctionReturn();
			Set<VariableExpression> requires = new ObjectOpenHashSet<>();
			Set<CachedUniform> brokenUniforms = new ObjectOpenHashSet<>();

			for (Map.Entry<String, Expression> entry : this.variablesExpressions.entrySet()) {
				requires.clear();

				entry.getValue().listVariables(requires);
				if (requires.isEmpty()) {
					continue;
				}

				CachedUniform uniform = this.variables.get(entry.getKey());

				List<CachedUniform> dependencies = new ArrayList<>();
				for (VariableExpression v : requires) {
					Expression evaluated = v.partialEval(this, functionReturn);
					if (evaluated instanceof CachedUniform) {
						dependencies.add((CachedUniform) evaluated);
					} else {
						// we are depending on a broken uniform
						brokenUniforms.add(uniform);
					}
				}

				if (dependencies.isEmpty()) {
					// can be empty if we rely on broken uniforms
					continue;
				}

				dependsOn.put(uniform, dependencies);
				dependsOnCount.put(uniform, dependencies.size());

				for (CachedUniform dependency : dependencies) {
					requiredBy.get(dependency).add(uniform);
				}
			}

			// actual toposort:
			List<CachedUniform> ordered = new ObjectArrayList<>();
			List<CachedUniform> free = new ObjectArrayList<>();

			// init
			for (CachedUniform entry : requiredBy.keySet()) {
				if (!dependsOnCount.containsKey(entry)) {
					free.add(entry);
				}
			}

			while (!free.isEmpty()) {
				CachedUniform pop = free.remove(free.size() - 1);
				if (!brokenUniforms.contains(pop)) {
					// only add those that aren't broken
					ordered.add(pop);
				} else {
					// mark all those that rely on use as broken too
					brokenUniforms.addAll(requiredBy.get(pop));
				}
				for (CachedUniform dependent : requiredBy.get(pop)) {
					int count = dependsOnCount.mergeInt(dependent, -1, Integer::sum);
					assert count >= 0;
					if (count == 0) {
						free.add(dependent);
						dependsOnCount.removeInt(dependent);
					}
				}
			}

			if (!brokenUniforms.isEmpty()) {
				Iris.logger.warn(
					"The following uniforms won't work, either because they are broken, or reference a broken uniform: \n" +
						brokenUniforms.stream().map(CachedUniform::getName).collect(Collectors.joining(", ")));
			}

			if (!dependsOnCount.isEmpty()) {
				throw new IllegalStateException("Circular reference detected between: " +
					dependsOnCount.object2IntEntrySet()
						.stream()
						.map(entry -> entry.getKey().getName() + " (" + entry.getIntValue() + ")")
						.collect(Collectors.joining(", "))
				);
			}

			this.orderedUniforms = ordered;
		}
	}

	private void addVariable(Expression expression, CachedUniform uniform) throws Exception {
		String name = uniform.getName();
		if (this.variables.containsKey(name))
			throw new Exception("Duplicated variable: " + name);
		if (this.inputHolder.containsKey(name))
			throw new Exception("Variable shadows build in uniform: " + name);

		this.variables.put(name, uniform);
		this.variablesExpressions.put(name, expression);
	}

	public void assignTo(LocationalUniformHolder targetHolder) {
		for (CachedUniform uniform : this.orderedUniforms) {
			if (!this.uniforms.contains(uniform)) {
				continue;
			}

			try {
				targetHolder.location(uniform.getName(), Type.convert(uniform.getType())).ifPresent(location -> {
					targetHolder.addUniform(UniformUpdateFrequency.CUSTOM, new PushedUniform(location, uniform));

					if (this.assigned.add(uniform)) {
						this.optimised = false;
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(uniform.getName(), e);
			}
		}
	}

	public void update() {
		if (!this.optimised) {
			this.optimise();
		}

		for (CachedUniform value : this.uniformOrder) {
			value.update();
		}
	}

	/**
	 * This function will do the following:
	 * <ul>
	 *     <li>
	 *         Remove unused uniforms
	 *     </li>
	 *     <li>
	 *         TODO: Create separate push lists for each renderpass
	 *     </li>
	 *     <li>
	 *         TODO: Sort the others in the correct execution line <p/>
	 *               note: that if a `EVERY_FRAME` depends on a `EVERY_TICK`, it has to correctly now that it's
	 *               dependency hasn't updated <br/>
	 *                  suggestion: set a boolean in the `EVERY_TICK` execution line saying this is a tick
	 *                  and have it set to false in the `EVERY_FRAME`. Depending on the value, `frameDependencies` or
	 *                  `allDependencies` lists are used
	 *     </li>
	 * </ul>
	 */
	private void optimise() {
		this.optimised = true;

		Object2IntMap<CachedUniform> dependedByCount = new Object2IntOpenHashMap<>();

		// Count the times a uniform is depended on
		for (List<CachedUniform> dependencies : this.dependsOn.values()) {
			for (CachedUniform dependency : dependencies) {
				dependedByCount.mergeInt(dependency, 1, Integer::sum);
			}
		}

		// Count the times a pass depends on a uniform
		// ensures they wont ever be removed
		for (CachedUniform cachedUniform : this.assigned) {
			dependedByCount.mergeInt(cachedUniform, 1, Integer::sum);
		}


		Set<CachedUniform> unused = new ObjectOpenHashSet<>();
		for (int i = this.orderedUniforms.size() - 1; i >= 0; i--) {
			CachedUniform uniform = this.orderedUniforms.get(i);
			if (!dependedByCount.containsKey(uniform)) {
				// not used
				unused.add(uniform);
				// remove dependencies
				List<CachedUniform> dependencies = this.dependsOn.get(uniform);
				if (dependencies != null) {
					for (CachedUniform dependency : dependencies) {
						// reduce count by 1
						dependedByCount.computeIntIfPresent(dependency, (key, value) -> value - 1);
					}
				}
			}
		}

		this.uniformOrder.clear();
		for (CachedUniform uniform : this.orderedUniforms) {
			if (!unused.contains(uniform)) {
				this.uniformOrder.add(uniform);
			}
		}
	}

	@Override
	public boolean hasVariable(String name) {
		return this.inputHolder.containsKey(name) || this.variables.containsKey(name);
	}

	@Override
	public Expression getVariable(String name) {
		// TODO: Make the simplify just return these ones
		final CachedUniform inputUniform = this.inputHolder.getUniform(name);
		if (inputUniform != null)
			return inputUniform;
		final CachedUniform customUniform = this.variables.get(name);
		if (customUniform != null)
			return customUniform;
		throw new RuntimeException("Unknown variable: " + name);
	}

	private static final class PushedUniform extends Uniform {
		private final CachedUniform uniform;
		private int lastPushedVersion;

		private PushedUniform(int location, CachedUniform uniform) {
			super(location);
			this.uniform = uniform;
		}

		@Override
		public void update() {
			// Versions start at 1, so a zeroed field means the uniform is pushed on the first bind.
			int version = this.uniform.getVersion();

			if (this.lastPushedVersion != version) {
				this.lastPushedVersion = version;
				this.uniform.push(this.location);
			}
		}
	}

	public static class Builder {
		final private static Map<String, Type> types = new ImmutableMap.Builder<String, Type>()
			.put("bool", Type.Boolean)
			.put("float", Type.Float)
			.put("int", Type.Int)
			.put("vec2", VectorType.VEC2)
			.put("vec3", VectorType.VEC3)
			.put("vec4", VectorType.VEC4)
			.build();
		Map<String, Variable> variables = new Object2ObjectLinkedOpenHashMap<>();

		public void addVariable(String type, String name, String expression, boolean isUniform) {
			if (variables.containsKey(name)) {
				Iris.logger.warn("Ignoring duplicated custom uniform name: " + name);
				return;
			}

			Type parsedType = types.get(type);
			if (parsedType == null) {
				Iris.logger.warn("Ignoring invalid uniform type: " + type + " of " + name);
				return;
			}

			try {
				ExpressionElement ast = Parser.parse(expression, IrisOptions.options);
				variables.put(name, new Variable(parsedType, name, ast, isUniform));
			} catch (Exception e) {
				Iris.logger.warn("Failed to parse custom variable/uniform " + name + " with expression " + expression, e);
			}
		}

		public CustomUniforms build(IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier) {
			Iris.logger.info("Starting custom uniform resolving");
			CustomUniformFixedInputUniformsHolder.Builder inputs = new CustomUniformFixedInputUniformsHolder.Builder();
			CommonUniforms.addNonDynamicUniforms(inputs, idMap, directives, updateNotifier);
			CustomUniformFixedInputUniformsHolder.Builder hardcoded = new CustomUniformFixedInputUniformsHolder.Builder();
			HardcodedCustomUniforms.addHardcodedCustomUniforms(hardcoded, updateNotifier);
			return new CustomUniforms(inputs.build(), hardcoded.build(), this.variables);
		}

		private record Variable(Type type, String name, ExpressionElement expression, boolean uniform) {
		}


	}
}
