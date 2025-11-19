package dev.openfeature.sdk;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * LayeredEvaluationContext implements EvaluationContext by layering multiple contexts:
 * API-level, Transaction-level, Client-level, Invocation-level, and Hook-level.
 * The contexts are checked in that order for values, with Hook-level having the highest precedence.
 */
public class LayeredEvaluationContext implements EvaluationContext {
    private final EvaluationContext apiContext;
    private final EvaluationContext transactionContext;
    private final EvaluationContext clientContext;
    private final EvaluationContext invocationContext;
    private final HashMap<String, Value> hookContext = new HashMap<>();

    private String targetingKey;
    private Set<String> keySet = null;

    /**
     * Constructor for LayeredEvaluationContext.
     */
    public LayeredEvaluationContext(
            EvaluationContext apiContext,
            EvaluationContext transactionContext,
            EvaluationContext clientContext,
            EvaluationContext invocationContext) {
        this.apiContext = apiContext;
        this.transactionContext = transactionContext;
        this.clientContext = clientContext;
        this.invocationContext = invocationContext;

        if (invocationContext != null && invocationContext.getTargetingKey() != null) {
            this.targetingKey = invocationContext.getTargetingKey();
        } else if (clientContext != null && clientContext.getTargetingKey() != null) {
            this.targetingKey = clientContext.getTargetingKey();
        } else if (transactionContext != null && transactionContext.getTargetingKey() != null) {
            this.targetingKey = transactionContext.getTargetingKey();
        } else if (apiContext != null && apiContext.getTargetingKey() != null) {
            this.targetingKey = apiContext.getTargetingKey();
        } else {
            this.targetingKey = null;
        }
    }

    @Override
    public String getTargetingKey() {
        return targetingKey;
    }

    /**
     * Using this method should be avoided as it comes with a performance cost.
     * Consider constructing a new LayeredEvaluationContext instead.
     *
     * <p>
     * Does not modify this object.
     *
     * @param overridingContext overriding context
     * @return A new LayeredEvaluationContext containing the context from this object, with the overridingContext
     *      merged on top.
     * @deprecated Use of this method is discouraged due to performance considerations.
     */
    @Deprecated
    @Override
    public EvaluationContext merge(EvaluationContext overridingContext) {
        var merged = new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
        merged.hookContext.putAll(this.hookContext);
        merged.hookContext.putAll(overridingContext.asMap());
        var otherTargetingKey = overridingContext.getTargetingKey();
        if (otherTargetingKey != null) {
            merged.targetingKey = otherTargetingKey;
        }
        return merged;
    }

    @Override
    public boolean isEmpty() {
        return hookContext.isEmpty()
                && (invocationContext == null || invocationContext.isEmpty())
                && (clientContext == null || clientContext.isEmpty())
                && (transactionContext == null || transactionContext.isEmpty())
                && (apiContext == null || apiContext.isEmpty());
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(ensureKeySet());
    }

    private Set<String> ensureKeySet() {
        if (this.keySet != null) {
            return this.keySet;
        }

        var keys = new HashSet<>(hookContext.keySet());

        if (invocationContext != null) {
            keys.addAll(invocationContext.keySet());
        }
        if (clientContext != null) {
            keys.addAll(clientContext.keySet());
        }
        if (transactionContext != null) {
            keys.addAll(transactionContext.keySet());
        }
        if (apiContext != null) {
            keys.addAll(apiContext.keySet());
        }
        this.keySet = keys;
        return keys;
    }

    private Value getFromContext(EvaluationContext context, String key) {
        if (context != null) {
            return context.getValue(key);
        }
        return null;
    }

    @Override
    public Value getValue(String key) {
        var hookValue = hookContext.get(key);
        if (hookValue != null) {
            return hookValue;
        }
        var invocationValue = getFromContext(invocationContext, key);
        if (invocationValue != null) {
            return invocationValue;
        }
        var clientValue = getFromContext(clientContext, key);
        if (clientValue != null) {
            return clientValue;
        }
        var transactionValue = getFromContext(transactionContext, key);
        if (transactionValue != null) {
            return transactionValue;
        }
        return getFromContext(apiContext, key);
    }

    @Override
    public Map<String, Value> asMap() {
        var keySet = ensureKeySet();
        var keys = keySet.size();
        if (keys == 0) {
            return new HashMap<>(1);
        }
        var map = new HashMap<String, Value>(keys);

        for (String key : keySet) {
            map.put(key, getValue(key));
        }
        return map;
    }

    @Override
    public Map<String, Value> asUnmodifiableMap() {
        var keySet = ensureKeySet();
        var keys = keySet.size();
        if (keys == 0) {
            return Collections.emptyMap();
        }
        var map = new HashMap<String, Value>(keys);

        for (String key : keySet) {
            map.put(key, getValue(key));
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Map<String, Object> asObjectMap() {
        var keySet = ensureKeySet();
        var keys = keySet.size();
        if (keys == 0) {
            return new HashMap<>(1);
        }
        var map = new HashMap<String, Object>(keys);

        for (String key : keySet) {
            map.put(key, convertValue(getValue(key)));
        }
        return map;
    }

    void putHookContext(Map<String, Value> context) {
        if (context == null) {
            return;
        }

        var targetingKey = context.get("targetingKey");
        if (targetingKey != null) {
            var targetingKeyStr = targetingKey.asString();
            if (targetingKeyStr != null) {
                this.targetingKey = targetingKeyStr;
                this.hookContext.put("targetingKey", targetingKey);
            }
        }
        this.hookContext.putAll(context);
    }
}
