package javasdk;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
public class HookEvaluation {
    private List<Hook> before = new ArrayList<>();
    private List<Hook> after = new ArrayList<>();
    private List<Hook> error = new ArrayList<>();
    private List<Hook> afterAll = new ArrayList<>();

    public void addBefore(Hook h) {
        before.add(h);
    }
    public void addAfter(Hook h) {
        after.add(h);
    }
    public void addError(Hook h) {
        error.add(h);
    }
    public void addAfterAll(Hook h) {
        afterAll.add(h);
    }
}
