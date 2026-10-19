description = "Hamcrest matchers and matcher decorators for HtmlElements and WebDriver elements"

dependencies {
    api(project(":htmlelements-java"))
    // Compile scope on purpose: ConditionMatcherDecorator (main source) throws
    // org.junit.AssumptionViolatedException, so JUnit 4 is part of this module's API.
    api(libs.junit)
    api(libs.hamcrest)

    testImplementation(libs.mockito.core)
}
