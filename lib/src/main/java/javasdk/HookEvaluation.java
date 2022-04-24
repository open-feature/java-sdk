package javasdk;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HookEvaluation<T> {
    private List<Hook<T>> before = new ArrayList<>();
    private List<Hook<T>> after = new ArrayList<>();
    private List<Hook<T>> error = new ArrayList<>();
    private List<Hook<T>> afterAll = new ArrayList<>();

    public void addBefore(Hook<T> h) {
        before.add(h);
    }
    public void addAfter(Hook<T> h) {
        after.add(h);
    }
    public void addError(Hook<T> h) {
        error.add(h);
    }
    public void addAfterAll(Hook<T> h) {
        afterAll.add(h);
    }
}
