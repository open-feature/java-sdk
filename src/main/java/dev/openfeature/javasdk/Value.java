package dev.openfeature.javasdk;

import java.time.Instant;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Values serve as a generic return type for structure data from providers.
 * Providers may deal in JSON, protobuf, XML or some other data-interchange format.
 * This intermediate representation provides a good medium of exchange.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class Value {

    private final Object innerObject;

    public Value() {
        this.innerObject = null; 
    }

    public Value(Object value) {
        this.innerObject = value; 
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
        this.innerObject = value.doubleValue(); 
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
        return this.innerObject instanceof Double;
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
     * Check if this Value represents a List.
     * 
     * @return boolean
     */
    public boolean isList() {
        return this.innerObject instanceof List;
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
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL",
        justification = "This is not a plain true/false method. It's understood it can return null.")
    public Boolean asBoolean() {
        if (this.isBoolean()) {
            return (Boolean)this.innerObject;
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
            return (String)this.innerObject;
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
        if (this.isNumber()) {
            return (int)Math.round((Double)this.innerObject);
        }
        return null;
    }
    
    /** 
     * Retrieve the underlying numeric value as a Double, or null.
     * 
     * @return Double
     */
    public Double asDouble() {
        if (this.isNumber()) {
            return (Double)this.innerObject;
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
            return (Structure)this.innerObject;
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
            return (Instant)this.innerObject;
        }
        return null;
    }
}
