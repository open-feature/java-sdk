package dev.openfeature.api.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DefaultHookDataTest {

    @Test
    void initialize() {
        DefaultHookData defaultHookData = new DefaultHookData();
        assertThat(defaultHookData.data).isNull();
    }

    @Test
    void setAndGet() {
        DefaultHookData defaultHookData = new DefaultHookData();
        defaultHookData.set("test", "test");
        assertThat(defaultHookData.data).isNotNull();
        assertThat(defaultHookData.get("test")).isEqualTo("test");
    }

    @Test
    void get() {
        DefaultHookData defaultHookData = new DefaultHookData();
        assertThat(defaultHookData.get("test")).isNull();
    }

    @Test
    void getType() {
        DefaultHookData defaultHookData = new DefaultHookData();
        defaultHookData.set("test", "test");
        assertThat(defaultHookData.data).isNotNull();
        assertThat(defaultHookData.get("test", String.class)).isEqualTo("test");
    }

    @Test
    void getWrongType() {
        DefaultHookData defaultHookData = new DefaultHookData();
        defaultHookData.set("test", "test");
        assertThat(defaultHookData.data).isNotNull();
        assertThatThrownBy(() -> defaultHookData.get("test", Integer.class)).isInstanceOf(ClassCastException.class);
    }

    @Test
    void getTypeNull() {
        DefaultHookData defaultHookData = new DefaultHookData();
        defaultHookData.set("other", "other");
        assertThat(defaultHookData.data).isNotNull();
        assertThat(defaultHookData.get("test", String.class)).isNull();
    }
}
