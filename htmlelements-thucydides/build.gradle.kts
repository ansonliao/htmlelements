description = "Serenity BDD (formerly Thucydides) integration for HtmlElements"

dependencies {
    api(project(":htmlelements-java"))
    api(libs.serenity.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.hamcrest)
}
