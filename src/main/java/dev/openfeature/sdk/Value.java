package dev.openfeature.sdk;

import static dev.openfeature.sdk.Structure.mapToStructure;

import dev.openfeature.sdk.exceptions.TypeMismatchError;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * Values serve as a generic return type for structure data from providers.
 * Providers may deal in JSON, protobuf, XML or some other data-interchange format.
 * This intermediate representation provides a good medium of exchange.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "checkstyle:MissingJavadocType", "checkstyle:NoFinalizer"})
public class Value implements Cloneable {

    private final Object innerObject;

    protected final void finalize() {
        // DO NOT REMOVE, spotbugs: CT_CONSTRUCTOR_THROW
    }

    /**
     * Construct a new null Value.
     */
    public Value() {
        this.innerObject = null;
    }

    /**
     * Construct a new Value with an Object.
     *
     * @param value to be wrapped.
     * @throws InstantiationException if value is not a valid type
     *                                (boolean, string, int, double, list, structure, instant)
     */
    public Value(Object value) throws InstantiationException {
        this.innerObject = value;
        if (!this.isNull()
                && !this.isBoolean()
                && !this.isString()
                && !this.isNumber()
                && !this.isStructure()
                && !this.isList()
                && !this.isInstant()) {
            throw new InstantiationException("Invalid value type: " + value.getClass());
        }
    }

    public Value(Value value) {
        this.innerObject = value.innerObject;
    }

    public Value(Boolean value) {
        this.innerObject = value;
    }

    public Value(String value) {
        this.innerObject = value;
    }

    public Value(Integer value) {
        this.innerObject = value;
    }

    public Value(Double value) {
        this.innerObject = value;
    }

    public Value(Structure value) {
        this.innerObject = value;
    }

    public Value(List<Value> value) {
        this.innerObject = value;
    }

    public Value(Instant value) {
        this.innerObject = value;
    }

    /**
     * Check if this Value represents null.
     *
     * @return boolean
     */
    public boolean isNull() {
        return this.innerObject == null;
    }

    /**
     * Check if this Value represents a Boolean.
     *
     * @return boolean
     */
    public boolean isBoolean() {
        return this.innerObject instanceof Boolean;
    }

    /**
     * Check if this Value represents a String.
     *
     * @return boolean
     */
    public boolean isString() {
        return this.innerObject instanceof String;
    }

    /**
     * Check if this Value represents a numeric value.
     *
     * @return boolean
     */
    public boolean isNumber() {
        return this.innerObject instanceof Number;
    }

    /**
     * Check if this Value represents a Structure.
     *
     * @return boolean
     */
    public boolean isStructure() {
        return this.innerObject instanceof Structure;
    }

    /**
     * Check if this Value represents a List of Values.
     *
     * @return boolean
     */
    public boolean isList() {
        if (!(this.innerObject instanceof List)) {
            return false;
        }

        List<?> list = (List<?>) this.innerObject;
        if (list.isEmpty()) {
            return true;
        }

        for (Object obj : list) {
            if (!(obj instanceof Value)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if this Value represents an Instant.
     *
     * @return boolean
     */
    public boolean isInstant() {
        return this.innerObject instanceof Instant;
    }

    /**
     * Retrieve the underlying Boolean value, or null.
     *
     * @return Boolean
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "NP_BOOLEAN_RETURN_NULL",
            justification = "This is not a plain true/false method. It's understood it can return null.")
    public Boolean asBoolean() {
        if (this.isBoolean()) {
            return (Boolean) this.innerObject;
        }
        return null;
    }

    /**
     * Retrieve the underlying object.
     *
     * @return Object
     */
    public Object asObject() {
        return this.innerObject;
    }

    /**
     * Retrieve the underlying String value, or null.
     *
     * @return String
     */
    public String asString() {
        if (this.isString()) {
            return (String) this.innerObject;
        }
        return null;
    }

    /**
     * Retrieve the underlying numeric value as an Integer, or null.
     * If the value is not an integer, it will be rounded using Math.round().
     *
     * @return Integer
     */
    public Integer asInteger() {
        if (this.isNumber() && !this.isNull()) {
            return ((Number) this.innerObject).intValue();
        }
        return null;
    }

    /**
     * Retrieve the underlying numeric value as a Double, or null.
     *
     * @return Double
     */
    public Double asDouble() {
        if (this.isNumber() && !isNull()) {
            return ((Number) this.innerObject).doubleValue();
        }
        return null;
    }

    /**
     * Retrieve the underlying Structure value, or null.
     *
     * @return Structure
     */
    public Structure asStructure() {
        if (this.isStructure()) {
            return (Structure) this.innerObject;
        }
        return null;
    }

    /**
     * Retrieve the underlying List value, or null.
     *
     * @return List
     */
    public List<Value> asList() {
        if (this.isList()) {
            //noinspection rawtypes,unchecked
            return (List) this.innerObject;
        }
        return null;
    }

    /**
     * Retrieve the underlying Instant value, or null.
     *
     * @return Instant
     */
    public Instant asInstant() {
        if (this.isInstant()) {
            return (Instant) this.innerObject;
        }
        return null;
    }

    /**
     * Perform deep clone of value object.
     *
     * @return Value
     */
    @SneakyThrows
    @Override
    protected Value clone() {
        if (this.isList()) {
            List<Value> copy = this.asList().stream().map(Value::new).collect(Collectors.toList());
            return new Value(copy);
        }
        if (this.isStructure()) {
            return new Value(new ImmutableStructure(this.asStructure().asUnmodifiableMap()));
        }
        if (this.isInstant()) {
            Instant copy = Instant.ofEpochMilli(this.asInstant().toEpochMilli());
            return new Value(copy);
        }
        return new Value(this.asObject());
    }

    /**
     * Wrap an object into a Value.
     *
     * @param object the object to wrap
     * @return the wrapped object
     */
    public static Value objectToValue(Object object) {
        if (object instanceof Value) {
            return (Value) object;
        } else if (object == null) {
            return new Value();
        } else if (object instanceof String) {
            return new Value((String) object);
        } else if (object instanceof Boolean) {
            return new Value((Boolean) object);
        } else if (object instanceof Integer) {
            return new Value((Integer) object);
        } else if (object instanceof Double) {
            return new Value((Double) object);
        } else if (object instanceof Structure) {
            return new Value((Structure) object);
        } else if (object instanceof List) {
            return new Value(
                    ((List<Object>) object).stream().map(o -> objectToValue(o)).collect(Collectors.toList()));
        } else if (object instanceof Instant) {
            return new Value((Instant) object);
        } else if (object instanceof Map) {
            return new Value(mapToStructure((Map<String, Object>) object));
        } else {
            throw new TypeMismatchError("Flag value " + object + " had unexpected type " + object.getClass() + ".");
        }
    }
}
