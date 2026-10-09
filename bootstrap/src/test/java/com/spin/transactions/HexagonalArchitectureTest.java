package com.spin.transactions;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.spin.transactions", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_application =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..application..")
                    .because("the domain must stay independent of application commands and use-case orchestration")
                    .allowEmptyShould(false);

    @ArchTest
    static final ArchRule domain_must_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                    .because("the domain must not import adapters or framework infrastructure")
                    .allowEmptyShould(false);

    @ArchTest
    static final ArchRule application_must_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                    .because("application services talk to the domain through ports, not adapters")
                    .allowEmptyShould(false);

    @ArchTest
    static final ArchRule domain_must_use_only_jdk_and_domain_types =
            classes().that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage("java..", "com.spin.transactions.domain..")
                    .because("the domain must use only the JDK and its own domain types");

    @ArchTest
    static final ArchRule application_must_use_only_jdk_application_and_domain_types =
            classes().that().resideInAPackage("..application..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "java..",
                            "com.spin.transactions.application..",
                            "com.spin.transactions.domain..")
                    .because("the application layer must remain JDK-only apart from the domain");
}
