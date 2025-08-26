package dev.openfeature.sdk.arch;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "dev.openfeature.sdk")
public class ArchitectureTest {

    @ArchTest
    public static final ArchRule avoidGetInstances = noClasses()
            .that()
            .resideOutsideOfPackages("..benchmark", "..e2e.*")
            .and()
            .haveSimpleNameNotEndingWith("SingeltonTest")
            .should()
            .callMethodWhere(describe(
                    "Avoid Internal usage of OpenFeatureAPI.GetInstances",
                    // Target method may not reside in class annotated with BusinessException
                    methodCall ->
                            methodCall.getTarget().getOwner().getFullName().equals("dev.openfeature.sdk.OpenFeatureAPI")
                                    // And target method may not have the static modifier
                                    && methodCall.getTarget().getName().equals("getInstance")));
}
