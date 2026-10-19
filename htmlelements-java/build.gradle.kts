description = "WebDriver PageObject extension: core element, loader and page factory support"

dependencies {
    api(libs.selenium.java)
    api(libs.commons.lang3)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.hamcrest)
}
