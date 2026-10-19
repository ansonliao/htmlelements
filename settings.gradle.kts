rootProject.name = "htmlelements-parent"

include(
    "htmlelements-java",
    "htmlelements-matchers",
    "htmlelements-thucydides",
    "htmlelements-all",
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
