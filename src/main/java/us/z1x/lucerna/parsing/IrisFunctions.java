package us.z1x.lucerna.parsing;

import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.function.AbstractTypedFunction;
import kroppeb.stareval.function.B2BFunction;
import kroppeb.stareval.function.BB2BFunction;
import kroppeb.stareval.function.F2FFunction;
import kroppeb.stareval.function.F2IFunction;
import kroppeb.stareval.function.FF2BFunction;
import kroppeb.stareval.function.FF2FFunction;
import kroppeb.stareval.function.FFF2BFunction;
import kroppeb.stareval.function.FFF2FFunction;
import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionResolver;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.I2IFunction;
import kroppeb.stareval.function.II2BFunction;
import kroppeb.stareval.function.II2IFunction;
import kroppeb.stareval.function.III2BFunction;
import kroppeb.stareval.function.III2IFunction;
import kroppeb.stareval.function.Type;
import kroppeb.stareval.function.TypedFunction;
import kroppeb.stareval.function.TypedFunction.Parameter;
import kroppeb.stareval.function.V2FFunction;
import kroppeb.stareval.function.V2IFunction;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.Arrays;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class IrisFunctions {
    public static final FunctionResolver functions;
    static final FunctionResolver.Builder builder = new FunctionResolver.Builder();

    static {
        {
            {
                IrisFunctions.<I2IFunction>addVectorizable("negate", (a) -> -a);
                IrisFunctions.<F2FFunction>add("negate", (a) -> -a);

                IrisFunctions.addUnaryOpJOML("negate", VectorType.VEC2, Vector2f::negate);
                IrisFunctions.addUnaryOpJOML("negate", VectorType.VEC3, Vector3f::negate);
                IrisFunctions.addUnaryOpJOML("negate", VectorType.VEC4, Vector4f::negate);
            }
        }
        {
            {
                IrisFunctions.<II2IFunction>addVectorizable("add", Integer::sum);
                IrisFunctions.<FF2FFunction>add("add", Float::sum);

                IrisFunctions.addBinaryOpJOML("add", VectorType.VEC2, Vector2f::add);
                IrisFunctions.addBinaryOpJOML("add", VectorType.VEC3, Vector3f::add);
                IrisFunctions.addBinaryOpJOML("add", VectorType.VEC4, Vector4f::add);
            }

            {
                IrisFunctions.<II2IFunction>addVectorizable("subtract", (a, b) -> a - b);
                IrisFunctions.<FF2FFunction>add("subtract", (a, b) -> a - b);

                IrisFunctions.addBinaryOpJOML("subtract", VectorType.VEC2, Vector2f::sub);
                IrisFunctions.addBinaryOpJOML("subtract", VectorType.VEC3, Vector3f::sub);
                IrisFunctions.addBinaryOpJOML("subtract", VectorType.VEC4, Vector4f::sub);
            }

            {
                IrisFunctions.<II2IFunction>addVectorizable("multiply", (a, b) -> a * b);
                IrisFunctions.<FF2FFunction>add("multiply", (a, b) -> a * b);

                IrisFunctions.addBinaryOpJOML("multiply", VectorType.VEC2, Vector2f::mul);
                IrisFunctions.addBinaryOpJOML("multiply", VectorType.VEC3, Vector3f::mul);
                IrisFunctions.addBinaryOpJOML("multiply", VectorType.VEC4, Vector4f::mul);

                IrisFunctions.<Vector2f>addScalarOpJOML("multiply", VectorType.VEC2, Vector2f::mul, true);
                IrisFunctions.<Vector3f>addScalarOpJOML("multiply", VectorType.VEC3, Vector3f::mul, true);
                IrisFunctions.<Vector4f>addScalarOpJOML("multiply", VectorType.VEC4, Vector4f::mul, true);
            }

            {
                IrisFunctions.<FF2FFunction>add("divide", (a, b) -> a / b);

                IrisFunctions.addBinaryOpJOML("divide", VectorType.VEC2, Vector2f::div);
                IrisFunctions.addBinaryOpJOML("divide", VectorType.VEC3, Vector3f::div);
                IrisFunctions.addBinaryOpJOML("divide", VectorType.VEC4, Vector4f::div);

                IrisFunctions.<Vector2f>addScalarOpJOML("divide", VectorType.VEC2, Vector2f::div, false);
                IrisFunctions.<Vector3f>addScalarOpJOML("divide", VectorType.VEC3, Vector3f::div, false);
                IrisFunctions.<Vector4f>addScalarOpJOML("divide", VectorType.VEC4, Vector4f::div, false);
            }

            {
                IrisFunctions.<II2IFunction>addVectorizable("remainder", (a, b) -> a % b);
                IrisFunctions.<FF2FFunction>add("remainder", (a, b) -> a % b);
            }
        }
        {
            IrisFunctions.<II2BFunction>addBooleanVectorizable("equals", (a, b) -> a == b);
            IrisFunctions.<FF2BFunction>add("equals", (a, b) -> a == b);

            IrisFunctions.addBinaryToBooleanOpJOML("equal", VectorType.VEC2, false, Vector2f::equals);
            IrisFunctions.addBinaryToBooleanOpJOML("equal", VectorType.VEC3, false, Vector3f::equals);
            IrisFunctions.addBinaryToBooleanOpJOML("equal", VectorType.VEC4, false, Vector4f::equals);

            IrisFunctions.<II2BFunction>addBooleanVectorizable("notEquals", (a, b) -> a != b);
            IrisFunctions.<FF2BFunction>add("notEquals", (a, b) -> a != b);

            IrisFunctions.addBinaryToBooleanOpJOML("equal", VectorType.VEC2, true, Vector2f::equals);
            IrisFunctions.addBinaryToBooleanOpJOML("equal", VectorType.VEC3, true, Vector3f::equals);
            IrisFunctions.addBinaryToBooleanOpJOML("equal", VectorType.VEC4, true, Vector4f::equals);

            IrisFunctions.<II2BFunction>add("lessThanOrEquals", (a, b) -> a <= b);
            IrisFunctions.<FF2BFunction>add("lessThanOrEquals", (a, b) -> a <= b);

            IrisFunctions.<II2BFunction>add("moreThanOrEquals", (a, b) -> a >= b);
            IrisFunctions.<FF2BFunction>add("moreThanOrEquals", (a, b) -> a >= b);

            IrisFunctions.<II2BFunction>add("lessThan", (a, b) -> a < b);
            IrisFunctions.<FF2BFunction>add("lessThan", (a, b) -> a < b);

            IrisFunctions.<II2BFunction>add("moreThan", (a, b) -> a > b);
            IrisFunctions.<FF2BFunction>add("moreThan", (a, b) -> a > b);
        }
        {

            IrisFunctions.<BB2BFunction>addVectorizable("equals", (a, b) -> a == b);
            IrisFunctions.<BB2BFunction>addVectorizable("notEquals", (a, b) -> a != b);
            IrisFunctions.<BB2BFunction>addVectorizable("and", (a, b) -> a && b);
            IrisFunctions.<BB2BFunction>addVectorizable("or", (a, b) -> a || b);
            IrisFunctions.<B2BFunction>addVectorizable("not", (a) -> !a);
        }

        {
            // these are also vectorizable in glsl, see page 4 of http://learnwebgl.brown37.net/12_shader_language/documents/webgl-reference-card-1_0.pdf

            {
                // optifine
                IrisFunctions.<F2FFunction>add("torad", (a) -> (float) Math.toRadians(a));
                IrisFunctions.<F2FFunction>add("todeg", (a) -> (float) Math.toDegrees(a));

                IrisFunctions.<F2FFunction>add("radians", (a) -> (float) Math.toRadians(a));
                IrisFunctions.<F2FFunction>add("degrees", (a) -> (float) Math.toDegrees(a));

                IrisFunctions.<F2FFunction>add("sin", (a) -> (float) Math.sin(a));
                IrisFunctions.<F2FFunction>add("cos", (a) -> (float) Math.cos(a));
                IrisFunctions.<F2FFunction>add("tan", (a) -> (float) Math.tan(a));
                IrisFunctions.<F2FFunction>add("asin", (a) -> (float) Math.asin(a));
                IrisFunctions.<F2FFunction>add("acos", (a) -> (float) Math.acos(a));
                IrisFunctions.<F2FFunction>add("atan", (a) -> (float) Math.atan(a));
                IrisFunctions.<FF2FFunction>add("atan", (y, x) -> (float) Math.atan2(y, x));
                // optifine
                IrisFunctions.<FF2FFunction>add("atan2", (y, x) -> (float) Math.atan2(y, x));
            }
            {
                IrisFunctions.<FF2FFunction>add("pow", (a, b) -> (float) Math.pow(a, b));
                IrisFunctions.<F2FFunction>add("exp", (a) -> (float) Math.exp(a));
                IrisFunctions.<F2FFunction>add("log", (a) -> (float) Math.log(a));
                // java has no built in exp2 or log2: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4851627
                IrisFunctions.<F2FFunction>add("exp2", (a) -> (float) Math.pow(2, a));
                IrisFunctions.<F2FFunction>add("log2", (a) -> (float) (Math.log(a) / Math.log(2)));

                IrisFunctions.<F2FFunction>add("sqrt", (a) -> (float) Math.sqrt(a));

                // optifine
                IrisFunctions.<F2FFunction>add("log10", (a) -> (float) Math.log10(a));

                IrisFunctions.<FF2FFunction>add("log",
                    (base, value) -> (float) (Math.log(value) / Math.log(base)));

                IrisFunctions.<F2FFunction>add("exp10", (a) -> (float) Math.pow(10, a));

            }

            {
                IrisFunctions.<I2IFunction>addVectorizable("abs", Math::abs);
                IrisFunctions.<F2FFunction>add("abs", Math::abs);

                IrisFunctions.addUnaryOpJOML("abs", VectorType.VEC2, Vector2f::absolute);
                IrisFunctions.addUnaryOpJOML("abs", VectorType.VEC3, Vector3f::absolute);
                IrisFunctions.addUnaryOpJOML("abs", VectorType.VEC4, Vector4f::absolute);

                IrisFunctions.<F2FFunction>add("sign", Math::signum);

                // optifine
                IrisFunctions.<F2FFunction>add("signum", Math::signum);

                // an int overload of floor avoids a float round trip that changes large results

                IrisFunctions.<F2FFunction>add("floor", (a) -> (float) Math.floor(a));
                IrisFunctions.<F2IFunction>add("floor", (a) -> (int) Math.floor(a));

                IrisFunctions.addUnaryOpJOML("floor", VectorType.VEC2, Vector2f::floor);
                IrisFunctions.addUnaryOpJOML("floor", VectorType.VEC3, Vector3f::floor);
                IrisFunctions.addUnaryOpJOML("floor", VectorType.VEC4, Vector4f::floor);

                IrisFunctions.<F2FFunction>add("ceil", (a) -> (float) Math.ceil(a));
                IrisFunctions.<F2IFunction>add("ceil", (a) -> (int) Math.ceil(a));

                IrisFunctions.addUnaryOpJOML("ceil", VectorType.VEC2, Vector2f::ceil);
                IrisFunctions.addUnaryOpJOML("ceil", VectorType.VEC3, Vector3f::ceil);
                IrisFunctions.addUnaryOpJOML("ceil", VectorType.VEC4, Vector4f::ceil);

                IrisFunctions.<F2FFunction>add("frac", (a) -> (float) (a - Math.floor(a)));

                IrisFunctions.<II2IFunction>addVectorizable("min", Math::min);
                IrisFunctions.<FF2FFunction>add("min", Math::min);

                IrisFunctions.addBinaryOpJOML("min", VectorType.VEC2, Vector2f::min);
                IrisFunctions.addBinaryOpJOML("min", VectorType.VEC3, Vector3f::min);
                IrisFunctions.addBinaryOpJOML("min", VectorType.VEC4, Vector4f::min);

                IrisFunctions.<II2IFunction>addVectorizable("max", Math::max);
                IrisFunctions.<FF2FFunction>add("max", Math::max);

                IrisFunctions.addBinaryOpJOML("max", VectorType.VEC2, Vector2f::max);
                IrisFunctions.addBinaryOpJOML("max", VectorType.VEC3, Vector3f::max);
                IrisFunctions.addBinaryOpJOML("max", VectorType.VEC4, Vector4f::max);

                {
                    // fake varargs
                    for (int length = 3; length <= 16; length++) {
                        {
                            Type[] inputs = new Type[length];
                            Arrays.fill(inputs, Type.Float);
                            IrisFunctions.add("min", new AbstractTypedFunction(Type.Float, inputs) {
                                @Override
                                public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                    params[0].evaluateTo(context, functionReturn);
                                    float min = functionReturn.floatReturn;
                                    for (int i = 1; i < params.length; i++) {
                                        params[1].evaluateTo(context, functionReturn);
                                        min = Math.min(min, functionReturn.floatReturn);
                                    }
                                    functionReturn.floatReturn = min;
                                }
                            });
                        }
                        {
                            Type[] inputs = new Type[length];
                            Arrays.fill(inputs, Type.Float);
                            IrisFunctions.add("max", new AbstractTypedFunction(Type.Float, inputs) {
                                @Override
                                public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                    params[0].evaluateTo(context, functionReturn);
                                    float max = functionReturn.floatReturn;
                                    for (int i = 1; i < params.length; i++) {
                                        params[1].evaluateTo(context, functionReturn);
                                        max = Math.max(max, functionReturn.floatReturn);
                                    }
                                    functionReturn.floatReturn = max;
                                }
                            });
                        }
                        {
                            Type[] inputs = new Type[length];
                            Arrays.fill(inputs, Type.Int);
                            IrisFunctions.addVectorizable("min", new AbstractTypedFunction(Type.Int, inputs) {
                                @Override
                                public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                    params[0].evaluateTo(context, functionReturn);
                                    int min = functionReturn.intReturn;
                                    for (int i = 1; i < params.length; i++) {
                                        params[1].evaluateTo(context, functionReturn);
                                        min = Math.min(min, functionReturn.intReturn);
                                    }
                                    functionReturn.intReturn = min;
                                }
                            });
                        }
                        {
                            Type[] inputs = new Type[length];
                            Arrays.fill(inputs, Type.Int);
                            IrisFunctions.addVectorizable("max", new AbstractTypedFunction(Type.Int, inputs) {
                                @Override
                                public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                    params[0].evaluateTo(context, functionReturn);
                                    int max = functionReturn.intReturn;
                                    for (int i = 1; i < params.length; i++) {
                                        params[1].evaluateTo(context, functionReturn);
                                        max = Math.max(max, functionReturn.intReturn);
                                    }
                                    functionReturn.intReturn = max;
                                }
                            });
                        }
                    }
                }

                // undefined when max < min
                IrisFunctions.<III2IFunction>addVectorizable("clamp",
                    (val, min, max) -> Math.max(min, Math.min(max, val)));
                IrisFunctions.<FFF2FFunction>add("clamp",
                    (val, min, max) -> Math.max(min, Math.min(max, val)));
                IrisFunctions.addTernaryOpJOML("clamp", VectorType.VEC2, (val, min, max, dest) -> {
                    val.min(max, dest);
                    dest.max(min);
                });
                IrisFunctions.addTernaryOpJOML("clamp", VectorType.VEC3, (val, min, max, dest) -> {
                    val.min(max, dest);
                    dest.max(min);
                });
                IrisFunctions.addTernaryOpJOML("clamp", VectorType.VEC4, (val, min, max, dest) -> {
                    val.min(max, dest);
                    dest.max(min);
                });

                IrisFunctions.<FFF2FFunction>add("mix", (x, y, a) -> x + (y - x) * a);

                IrisFunctions.<II2IFunction>addVectorizable("edge", (edge, x) -> (x < edge) ? 0 : 1);
                IrisFunctions.<FF2FFunction>add("edge", (edge, x) -> (x < edge) ? 0 : 1);
            }

            {
            }

            {
            }

            {
            }

            {
                {
                    IrisFunctions.<II2IFunction>addVectorizable("fmod", Math::floorMod);
                    IrisFunctions.<FF2FFunction>add("fmod", (a, b) -> (a % b + b) % b);
                }
                {
                    Random random = new Random();
                    // randomInt(), randomInt(int bound), randomInt(int inclusiveMin, int exclusiveMax)
                    IrisFunctions.<V2IFunction>addVectorizable("randomInt", random::nextInt);
                    IrisFunctions.<I2IFunction>addVectorizable("randomInt", random::nextInt);
                    IrisFunctions.<II2IFunction>addVectorizable("randomInt", (a, b) -> random.nextInt(b - a) + a);

                    // random, random(float min, float max)
                    IrisFunctions.<V2FFunction>add("random", random::nextFloat);
                    IrisFunctions.<FF2FFunction>add("random", (min, max) ->
                        min + random.nextFloat() * (max - min));
                }
                {
                    // if(boolean, primitive, primitive) -> primitive and if(boolean, xvec, xvec) -> xvec
                    for (Type.Primitive type : Type.AllPrimitives) {
                        add("if", new AbstractTypedFunction(type, new Type[]{Type.Boolean, type, type}) {
                            @Override
                            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                params[0].evaluateTo(context, functionReturn);

                                params[
                                    functionReturn.booleanReturn ? 1 : 2
                                    ].evaluateTo(context, functionReturn);

                            }
                        });
                    }

                    for (Type type : VectorType.AllVectorTypes) {
                        add("if", new AbstractTypedFunction(type, new Type[]{Type.Boolean, type, type}) {
                            @Override
                            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                params[0].evaluateTo(context, functionReturn);

                                params[
                                    functionReturn.booleanReturn ? 1 : 2
                                    ].evaluateTo(context, functionReturn);

                            }
                        });
                    }

                    {
                        // fake varargs
                        for (int length = 2; length <= 16; length++) {
                            for (Type.Primitive type : Type.AllPrimitives) {
                                Type[] params = new Type[length * 2 + 1];
                                for (int i = 0; i < length * 2; i += 2) {
                                    params[i] = Type.Boolean;
                                    params[i + 1] = type;
                                }
                                params[length * 2] = type;
                                int finalLength = length * 2;
                                add("if", new AbstractTypedFunction(type, params) {
                                    @Override
                                    public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                        for (int i = 0; i < finalLength; i += 2) {
                                            params[i].evaluateTo(context, functionReturn);
                                            if (functionReturn.booleanReturn) {
                                                params[i + 1].evaluateTo(context, functionReturn);
                                                return;
                                            }
                                            params[finalLength].evaluateTo(context, functionReturn);
                                        }
                                    }
                                });
                            }

                            for (Type type : VectorType.AllVectorTypes) {
                                add("if", new AbstractTypedFunction(type, new Type[]{Type.Boolean, type, type}) {
                                    @Override
                                    public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                        params[0].evaluateTo(context, functionReturn);

                                        params[
                                            functionReturn.booleanReturn ? 1 : 2
                                            ].evaluateTo(context, functionReturn);

                                    }
                                });
                            }
                        }
                    }
                }
                {
                    // smooth(target)
                    builder.addDynamicFunction("smooth", Type.Float, () ->
                        new AbstractTypedFunction(
                            Type.Float,
                            new Parameter[]{
                                new Parameter(Type.Float, false),
                            },
                            0,
                            false
                        ) {
                            private final SmoothFloat smoothFloat = new SmoothFloat();

                            @Override
                            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                params[0].evaluateTo(context, functionReturn);
                                float target = functionReturn.floatReturn;
                                functionReturn.floatReturn = smoothFloat.updateAndGet(
                                    target,
                                    1,
                                    1
                                );
                            }
                        });

                    // smooth(id, target)
                    builder.addDynamicFunction("smooth", Type.Float, () ->
                        new AbstractTypedFunction(
                            Type.Float,
                            new Parameter[]{
                                new Parameter(Type.Float, true), // id for backward compat with optifine
                                new Parameter(Type.Float, false),
                            },
                            1,
                            false
                        ) {
                            private final SmoothFloat smoothFloat = new SmoothFloat();

                            @Override
                            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                params[1].evaluateTo(context, functionReturn);
                                float target = functionReturn.floatReturn;
                                functionReturn.floatReturn = smoothFloat.updateAndGet(
                                    target,
                                    1,
                                    1
                                );
                            }
                        });

                    // smooth(target, fadeTime)
                    builder.addDynamicFunction("smooth", Type.Float, () ->
                        new AbstractTypedFunction(
                            Type.Float,
                            new Parameter[]{
                                new Parameter(Type.Float, false),
                                new Parameter(Type.Float, false),
                            },
                            0,
                            false
                        ) {
                            private final SmoothFloat smoothFloat = new SmoothFloat();

                            @Override
                            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                params[0].evaluateTo(context, functionReturn);
                                float target = functionReturn.floatReturn;

                                params[1].evaluateTo(context, functionReturn);
                                float fadeTime = functionReturn.floatReturn;

                                functionReturn.floatReturn = smoothFloat.updateAndGet(
                                    target,
                                    fadeTime,
                                    fadeTime
                                );
                            }
                        });

                    // smooth(id, target, fadeTime)
                    builder.addDynamicFunction("smooth", Type.Float, () ->
                        new AbstractTypedFunction(
                            Type.Float,
                            new Parameter[]{
                                new Parameter(Type.Float, true), // id for backward compat with optifine
                                new Parameter(Type.Float, false),
                                new Parameter(Type.Float, false),
                            },
                            1,
                            false
                        ) {
                            private final SmoothFloat smoothFloat = new SmoothFloat();

                            @Override
                            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                params[1].evaluateTo(context, functionReturn);
                                float target = functionReturn.floatReturn;

                                params[2].evaluateTo(context, functionReturn);
                                float fadeTime = functionReturn.floatReturn;

                                functionReturn.floatReturn = smoothFloat.updateAndGet(
                                    target,
                                    fadeTime,
                                    fadeTime
                                );
                            }
                        });

                    // smooth(target, fadeUpTime, fadeDownTime)
                    builder.addDynamicFunction("smooth", Type.Float, () ->
                        new AbstractTypedFunction(
                            Type.Float,
                            new Parameter[]{
                                new Parameter(Type.Float, false),
                                new Parameter(Type.Float, false),
                                new Parameter(Type.Float, false),
                            },
                            0,
                            false
                        ) {
                            private final SmoothFloat smoothFloat = new SmoothFloat();

                            @Override
                            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                params[0].evaluateTo(context, functionReturn);
                                float target = functionReturn.floatReturn;

                                params[1].evaluateTo(context, functionReturn);
                                float fadeUpTime = functionReturn.floatReturn;
                                params[2].evaluateTo(context, functionReturn);
                                float fadeDownTime = functionReturn.floatReturn;

                                functionReturn.floatReturn = smoothFloat.updateAndGet(
                                    target,
                                    fadeUpTime,
                                    fadeDownTime
                                );
                            }
                        });

                    // smooth(id, target, fadeUpTime, fadeDownTime)
                    builder.addDynamicFunction("smooth", Type.Float, () ->
                        new AbstractTypedFunction(
                            Type.Float,
                            new Parameter[]{
                                new Parameter(Type.Float, true), // id for backward compat with optifine
                                new Parameter(Type.Float, false),
                                new Parameter(Type.Float, false),
                                new Parameter(Type.Float, false),
                            },
                            1,
                            false
                        ) {
                            private final SmoothFloat smoothFloat = new SmoothFloat();

                            @Override
                            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                params[1].evaluateTo(context, functionReturn);
                                float target = functionReturn.floatReturn;

                                params[2].evaluateTo(context, functionReturn);
                                float fadeUpTime = functionReturn.floatReturn;
                                params[3].evaluateTo(context, functionReturn);
                                float fadeDownTime = functionReturn.floatReturn;

                                functionReturn.floatReturn = smoothFloat.updateAndGet(
                                    target,
                                    fadeUpTime,
                                    fadeDownTime
                                );
                            }
                        });
                }
            }
        }

        {
            addImplicitCast(Type.Int, Type.Float, r -> {
                r.floatReturn = r.intReturn;
            });
            addExplicitCast(Type.Float, Type.Int, r -> {
                r.intReturn = (int) r.floatReturn;
            });
        }

        {
            IrisFunctions.<III2BFunction>add("between", (a, min, max) -> a >= min && a <= max);
            IrisFunctions.<FFF2BFunction>add("between", (a, min, max) -> a >= min && a <= max);

            IrisFunctions.<FFF2BFunction>add("equals", (a, b, epsilon) -> Math.abs(a - b) <= epsilon);

            {
                // fake varargs
                for (int length = 2; length <= 32; length++) {
                    Type[] params = new Type[length];
                    Arrays.fill(params, Type.Float);
                    int finalLength = length;
                    IrisFunctions.add("in", new AbstractTypedFunction(
                        Type.Boolean,
                        params
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            float value = functionReturn.floatReturn;
                            for (int i = 1; i < finalLength; i++) {
                                params[i].evaluateTo(context, functionReturn);
                                if (functionReturn.floatReturn == value) {
                                    functionReturn.booleanReturn = true;
                                    return;
                                }
                            }
                            functionReturn.booleanReturn = false;
                        }
                    });
                }
            }
        }

        {
            for (Type.Primitive type : new Type.Primitive[]{Type.Boolean, Type.Int}) {
                for (int size = 2; size <= 4; size++) {
                    TypedFunction function = new VectorConstructor(type, size);
                    add(
                        Character.toLowerCase(
                            type.getClass().getSimpleName().charAt(0)
                        ) + "vec" + size, function);
                }
            }

            add("vec2", new AbstractTypedFunction(
                VectorType.VEC2,
                new Type[]{Type.Float, Type.Float}
            ) {
                @Override
                public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                    params[0].evaluateTo(context, functionReturn);
                    float x = functionReturn.floatReturn;

                    params[1].evaluateTo(context, functionReturn);
                    float y = functionReturn.floatReturn;

                    functionReturn.objectReturn = new Vector2f(x, y);
                }
            });

            add("vec3", new AbstractTypedFunction(
                VectorType.VEC3,
                new Type[]{Type.Float, Type.Float, Type.Float}
            ) {
                @Override
                public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                    params[0].evaluateTo(context, functionReturn);
                    float x = functionReturn.floatReturn;

                    params[1].evaluateTo(context, functionReturn);
                    float y = functionReturn.floatReturn;

                    params[2].evaluateTo(context, functionReturn);
                    float z = functionReturn.floatReturn;

                    functionReturn.objectReturn = new Vector3f(x, y, z);
                }
            });

            add("vec4", new AbstractTypedFunction(
                VectorType.VEC4,
                new Type[]{Type.Float, Type.Float, Type.Float, Type.Float}
            ) {
                @Override
                public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                    params[0].evaluateTo(context, functionReturn);
                    float x = functionReturn.floatReturn;

                    params[1].evaluateTo(context, functionReturn);
                    float y = functionReturn.floatReturn;

                    params[2].evaluateTo(context, functionReturn);
                    float z = functionReturn.floatReturn;

                    params[3].evaluateTo(context, functionReturn);
                    float w = functionReturn.floatReturn;

                    functionReturn.objectReturn = new Vector4f(x, y, z, w);
                }
            });
        }

        {
            String[][] accessNames = new String[][]{
                new String[]{"0", "r", "x", "s"},
                new String[]{"1", "g", "y", "t"},
                new String[]{"2", "b", "z", "p"},
                new String[]{"3", "a", "w", "q"}
            };

            {
                for (String access : accessNames[0]) {
                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Float,
                        new Type[]{VectorType.VEC2}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.floatReturn = ((Vector2f) functionReturn.objectReturn).x;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Int,
                        new Type[]{VectorType.I_VEC2}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.intReturn = ((Vector2i) functionReturn.objectReturn).x;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Float,
                        new Type[]{VectorType.VEC3}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.floatReturn = ((Vector3f) functionReturn.objectReturn).x;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Int,
                        new Type[]{VectorType.I_VEC3}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.intReturn = ((Vector3i) functionReturn.objectReturn).x;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Float,
                        new Type[]{VectorType.VEC4}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.floatReturn = ((Vector4f) functionReturn.objectReturn).x;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Int,
                        new Type[]{VectorType.I_VEC4}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.intReturn = ((Vector4i) functionReturn.objectReturn).x;
                        }
                    });
                }
            }

            {
                for (String access : accessNames[1]) {
                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Float,
                        new Type[]{VectorType.VEC2}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.floatReturn = ((Vector2f) functionReturn.objectReturn).y;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Int,
                        new Type[]{VectorType.I_VEC2}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.intReturn = ((Vector2i) functionReturn.objectReturn).y;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Float,
                        new Type[]{VectorType.VEC3}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.floatReturn = ((Vector3f) functionReturn.objectReturn).y;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Int,
                        new Type[]{VectorType.I_VEC3}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.intReturn = ((Vector3i) functionReturn.objectReturn).y;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Float,
                        new Type[]{VectorType.VEC4}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.floatReturn = ((Vector4f) functionReturn.objectReturn).y;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Int,
                        new Type[]{VectorType.I_VEC4}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.intReturn = ((Vector4i) functionReturn.objectReturn).y;
                        }
                    });
                }
            }

            {
                for (String access : accessNames[2]) {
                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Float,
                        new Type[]{VectorType.VEC3}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.floatReturn = ((Vector3f) functionReturn.objectReturn).z;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Int,
                        new Type[]{VectorType.I_VEC3}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.intReturn = ((Vector3i) functionReturn.objectReturn).z;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Float,
                        new Type[]{VectorType.VEC4}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.floatReturn = ((Vector4f) functionReturn.objectReturn).z;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Int,
                        new Type[]{VectorType.I_VEC4}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.intReturn = ((Vector4i) functionReturn.objectReturn).z;
                        }
                    });
                }
            }

            {
                for (String access : accessNames[3]) {
                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Float,
                        new Type[]{VectorType.VEC4}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.floatReturn = ((Vector4f) functionReturn.objectReturn).w;
                        }
                    });

                    IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                        Type.Int,
                        new Type[]{VectorType.I_VEC4}
                    ) {
                        @Override
                        public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                            params[0].evaluateTo(context, functionReturn);
                            functionReturn.intReturn = ((Vector4i) functionReturn.objectReturn).w;
                        }
                    });
                }
            }

            {
                for (int i = 0; i < 4; i++) {
                    for (String access : accessNames[i]) {
                        int finalI = i;
                        IrisFunctions.add("<access$" + access + ">", new AbstractTypedFunction(
                            VectorType.VEC4,
                            new Type[]{MatrixType.MAT4}
                        ) {
                            @Override
                            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                                params[0].evaluateTo(context, functionReturn);
                                functionReturn.objectReturn = ((Matrix4f) functionReturn.objectReturn).getColumn(finalI, new Vector4f());
                            }
                        });
                    }
                }
            }
        }

        functions = builder.build();
    }

    static <T extends TypedFunction> void addVectorized(String name, T function) {
        if (function.getReturnType() instanceof Type.Primitive) {
            add(name, new VectorizedFunction(function, 2));
            add(name, new VectorizedFunction(function, 3));
            add(name, new VectorizedFunction(function, 4));
        } else {
            throw new IllegalArgumentException(name + " is not vectorizable");
        }
    }

    static <T extends TypedFunction> void addVectorizable(String name, T function) {
        add(name, function);
        addVectorized(name, function);
    }

    static <T extends TypedFunction> void addBooleanVectorizable(String name, T function) {
        assert function.getReturnType().equals(Type.Boolean);
        add(name, function);
        if (function.getReturnType() instanceof Type.Primitive) {
            add(name, new BooleanVectorizedFunction(function, 2));
            add(name, new BooleanVectorizedFunction(function, 3));
            add(name, new BooleanVectorizedFunction(function, 4));
        } else {
            throw new IllegalArgumentException(name + " is not vectorizable");
        }
    }

    static <T> void addUnaryOpJOML(String name, VectorType.JOMLVector<T> type, BiConsumer<T, T> function) {
        builder.add(name, new AbstractTypedFunction(
            type,
            new Type[]{type}
        ) {
            final private T vector = type.create();

            @SuppressWarnings("unchecked")
            @Override
            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                params[0].evaluateTo(context, functionReturn);
                T a = (T) functionReturn.objectReturn;

                function.accept(a, this.vector);
                functionReturn.objectReturn = this.vector;
            }
        });
    }

    static <T> void addBinaryOpJOML(String name, VectorType.JOMLVector<T> type, TriConsumer<T, T, T> function) {
        builder.add(name, new AbstractTypedFunction(
            type,
            new Type[]{type, type}
        ) {
            final private T vector = type.create();

            @SuppressWarnings("unchecked")
            @Override
            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                params[0].evaluateTo(context, functionReturn);
                T a = (T) functionReturn.objectReturn;

                params[1].evaluateTo(context, functionReturn);
                T b = (T) functionReturn.objectReturn;

                function.accept(a, b, this.vector);
                functionReturn.objectReturn = this.vector;
            }
        });
    }

    // vector with scalar, and scalar with vector when the operation commutes
    static <T> void addScalarOpJOML(String name, VectorType.JOMLVector<T> type, ScalarOp<T> function, boolean commutes) {
        builder.add(name, new AbstractTypedFunction(type, new Type[]{type, Type.Float}) {
            final private T vector = type.create();

            @SuppressWarnings("unchecked")
            @Override
            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                params[1].evaluateTo(context, functionReturn);
                float b = functionReturn.floatReturn;

                params[0].evaluateTo(context, functionReturn);
                function.accept((T) functionReturn.objectReturn, b, this.vector);
                functionReturn.objectReturn = this.vector;
            }
        });

        if (commutes) {
            builder.add(name, new AbstractTypedFunction(type, new Type[]{Type.Float, type}) {
                final private T vector = type.create();

                @SuppressWarnings("unchecked")
                @Override
                public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                    params[0].evaluateTo(context, functionReturn);
                    float a = functionReturn.floatReturn;

                    params[1].evaluateTo(context, functionReturn);
                    function.accept((T) functionReturn.objectReturn, a, this.vector);
                    functionReturn.objectReturn = this.vector;
                }
            });
        }
    }

    static <T> void addTernaryOpJOML(String name, VectorType.JOMLVector<T> type, QuadConsumer<T, T, T, T> function) {
        builder.add(name, new AbstractTypedFunction(
            type,
            new Type[]{type, type, type}
        ) {
            final private T vector = type.create();

            @SuppressWarnings("unchecked")
            @Override
            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                params[0].evaluateTo(context, functionReturn);
                T a = (T) functionReturn.objectReturn;

                params[1].evaluateTo(context, functionReturn);
                T b = (T) functionReturn.objectReturn;

                params[2].evaluateTo(context, functionReturn);
                T c = (T) functionReturn.objectReturn;

                function.accept(a, b, c, this.vector);
                functionReturn.objectReturn = this.vector;
            }
        });
    }

    static <T> void addBinaryToBooleanOpJOML(
        String name,
        VectorType.JOMLVector<T> type,
        boolean inverted,
        ObjectObject2BooleanFunction<T, T> function) {
        builder.add(name, new AbstractTypedFunction(
            type,
            new Type[]{type, type}
        ) {
            @SuppressWarnings("unchecked")
            @Override
            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                params[0].evaluateTo(context, functionReturn);
                T a = (T) functionReturn.objectReturn;

                params[1].evaluateTo(context, functionReturn);
                T b = (T) functionReturn.objectReturn;

                functionReturn.objectReturn = function.apply(a, b) != inverted;
            }
        });
    }

    static <T extends TypedFunction> void add(String name, T function) {
        builder.add(name, function);
    }

    static void addCast(final String name, final Type from, final Type to, final Consumer<FunctionReturn> function) {
        add(name, new TypedFunction() {
            @Override
            public Type getReturnType() {
                return to;
            }

            @Override
            public Parameter[] getParameters() {
                return new Parameter[]{new Parameter(from)};
            }

            @Override
            public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
                params[0].evaluateTo(context, functionReturn);
                function.accept(functionReturn);
            }
        });
    }

    static void addImplicitCast(final Type from, final Type to, final Consumer<FunctionReturn> function) {
        addCast("<cast>", from, to, function);
        addExplicitCast(from, to, function);
    }

    static void addExplicitCast(final Type from, final Type to, final Consumer<FunctionReturn> function) {
        addCast("to" + to.getClass().getSimpleName(), from, to, function);
    }

    interface ObjectObject2BooleanFunction<T, U> {
        boolean apply(T t, U u);
    }

    interface ScalarOp<T> {
        void accept(T vector, float scalar, T dest);
    }

    interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    interface QuadConsumer<T, U, V, W> {
        void accept(T t, U u, V v, W w);
    }
}

