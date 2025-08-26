// Quick test to verify the refactoring worked
import dev.openfeature.api.OpenFeatureAPI;
// These should NOT be directly accessible to external users:
// import dev.openfeature.api.NoOpOpenFeatureAPI; // Should be package-private
// import dev.openfeature.api.internal.noop.NoOpClient; // Should be in internal package
// import dev.openfeature.api.internal.noop.NoOpProvider; // Should be in internal package
// import dev.openfeature.api.internal.noop.NoOpTransactionContextPropagator; // Should be in internal package

public class test_noop_access {
    public static void main(String[] args) {
        // This should work - getting API instance
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        System.out.println("API instance retrieved: " + api.getClass().getSimpleName());
        
        // This should work - using the client
        var client = api.getClient();
        System.out.println("Client retrieved: " + client.getClass().getSimpleName());
        
        // This should work - getting a boolean flag
        boolean result = client.getBooleanValue("test-flag", false);
        System.out.println("Flag evaluation result: " + result);
        
        System.out.println("Refactoring verification complete!");
    }
}