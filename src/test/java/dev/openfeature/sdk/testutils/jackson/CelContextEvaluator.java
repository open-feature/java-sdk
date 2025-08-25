package dev.openfeature.sdk.testutils.jackson;

import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.providers.memory.ContextEvaluator;
import dev.openfeature.sdk.providers.memory.Flag;
import java.util.HashMap;
import java.util.Map;

public class CelContextEvaluator<T> implements ContextEvaluator<T> {
    private final CelRuntime.Program program;

    public CelContextEvaluator(String expression) {
        try {
            CelRuntime celRuntime =
                    CelRuntimeFactory.standardCelRuntimeBuilder().build();
            CelCompiler celCompiler = CelCompilerFactory.standardCelCompilerBuilder()
                    .addVar("customer", SimpleType.STRING)
                    .addVar("email", SimpleType.STRING)
                    .addVar("dummy", SimpleType.STRING)
                    .setResultType(SimpleType.STRING)
                    // Add other variables you expect
                    .build();

            var ast = celCompiler.compile(expression).getAst();
            this.program = celRuntime.createProgram(ast);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compile CEL expression: " + expression, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T evaluate(Flag flag, EvaluationContext evaluationContext) {
            Map<String, Object> objectMap = new HashMap<>();
            // Provide defaults for all declared variables to prevent runtime errors.
            objectMap.put("email", "");
            objectMap.put("customer", "");
            objectMap.put("dummy", "");

            if (evaluationContext != null) {
                // Evaluate with context, overriding defaults.
                objectMap.putAll(evaluationContext.asObjectMap());
            }
            }

            Object result = program.eval(objectMap);

            String stringResult = (String) result;
            return (T) flag.getVariants().get(stringResult);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
