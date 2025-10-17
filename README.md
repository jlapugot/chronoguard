# ChronoGuard

[![CI/CD](https://github.com/jlapugot/chronoguard/actions/workflows/maven-ci-cd.yml/badge.svg)](https://github.com/jlapugot/chronoguard/actions/workflows/maven-ci-cd.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jlapugot.chronoguard/chronoguard.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.jlapugot.chronoguard%22)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

ChronoGuard is a powerful, zero-code time-warping test library for Java and JUnit 5. It lets you control time in your unit and integration tests, allowing you to freeze the clock, travel forwards or backwards, and test time-dependent logic like token expiration or cache TTLs with simple annotations and a clean API. No application code changes required.

## Features

- **Time Travel**: Move time forward or backward with simple `Duration` objects.
- **Time Freezing**: Freeze the clock at a specific `Instant` or `ZonedDateTime`.
- **Declarative API**: Use the `@TimeWarp` annotation in JUnit 5 to set the time for a specific test without writing any code.
- **Programmatic API**: Use the static `TimeController` class for fine-grained, dynamic control over time within your test methods.
- **Seamless Integration**: Works out-of-the-box by manipulating system-level time calls at the bytecode level. No need to modify your application's source code.
- **Framework Friendly**: Perfect for testing time-sensitive logic in any Java application, including Spring Boot.

## Setup

### Maven

Add the dependencies to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.jlapugot.chronoguard</groupId>
        <artifactId>chronoguard-junit5</artifactId>
        <version>1.1.0</version> <!-- Replace with the latest version -->
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.github.jlapugot.chronoguard</groupId>
        <artifactId>chronoguard-agent</artifactId>
        <version>1.1.0</version> <!-- Replace with the latest version -->
        <scope>test</scope>
    </dependency>
</dependencies>
```

Then, configure the `maven-surefire-plugin` to attach the Java Agent, which is required for time manipulation:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0</version>
            <configuration>
                <argLine>
                    -javaagent:${settings.localRepository}/io/github/jlapugot/chronoguard/chronoguard-agent/${project.version}/chronoguard-agent-${project.version}.jar
                </argLine>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Gradle

Add the dependencies to your `build.gradle` or `build.gradle.kts`:

**Groovy DSL:**
```groovy
testImplementation 'io.github.jlapugot.chronoguard:chronoguard-junit5:1.0.1' // Replace with the latest version
testImplementation 'io.github.jlapugot.chronoguard:chronoguard-agent:1.0.1' // Replace with the latest version
```

**Kotlin DSL:**
```kotlin
testImplementation("io.github.jlapugot.chronoguard:chronoguard-junit5:1.0.1") // Replace with the latest version
testImplementation("io.github.jlapugot.chronoguard:chronoguard-agent:1.0.1") // Replace with the latest version
```

Then, configure the `test` task to attach the Java Agent:

```groovy
test {
    jvmArgs "-javaagent:${configurations.testRuntimeClasspath.find { it.name.contains('chronoguard-agent') }}"
}
```

## Usage

To get started, simply add the `ChronoGuardExtension` to your test class. This ensures that time is automatically reset after each test.

```java
@ExtendWith(ChronoGuardExtension.class)
class MyTest {
    // ... your tests
}
```

### Programmatic Control with `TimeController`

You can directly manipulate time within a test method using the static methods on `TimeController`.

**Example: Testing Cache Expiration**

```java
@ExtendWith(ChronoGuardExtension.class)
class CacheTest {

    private final UserCache cache = new UserCache(); // A cache with a 5-minute TTL

    @Test
    void testCacheExpiration() {
        // 1. Add an item to the cache
        cache.put("user1", new User("Alice"));
        assertThat(cache.get("user1")).isNotNull();

        // 2. Travel 4 minutes into the future
        TimeController.travel(Duration.ofMinutes(4));
        assertThat(cache.get("user1")).isNotNull(); // Still there

        // 3. Travel 2 more minutes (total 6 minutes)
        TimeController.travel(Duration.ofMinutes(2));
        assertThat(cache.get("user1")).isNull(); // Expired!
    }
}
```

### Declarative Control with `@TimeWarp`

For tests that need to run at a fixed point in time, the `@TimeWarp` annotation is the easiest approach.

**Example: Testing a Token at a Specific Time**

```java
@ExtendWith(ChronoGuardExtension.class)
class TokenTest {

    @Autowired
    private TokenService tokenService;

    @Test
    @TimeWarp(freezeAt = "2025-01-15T10:00:00Z")
    void testAtSpecificTime() {
        // Inside this test, the time is frozen at 2025-01-15 10:00 AM UTC
        String token = tokenService.createToken("user456");

        // Verify the token's expiration is exactly 1 hour later
        Instant expiration = tokenService.getExpiration(token);
        assertThat(expiration).isEqualTo(Instant.parse("2025-01-15T11:00:00Z"));
    }
}
```

### Combining Approaches

You can also combine freezing and traveling for more complex scenarios, like testing scheduled jobs.

**Example: Testing a Scheduled Job**

```java
@ExtendWith(ChronoGuardExtension.class)
class ScheduledJobTest {

    @Autowired
    private DailyReportService reportService; // A service with a job scheduled for 2 AM

    @Test
    void testDailyReportGeneration() {
        // 1. Freeze time at the beginning of the day
        TimeController.freeze(Instant.parse("2025-10-16T00:00:00Z"));

        // 2. Run the scheduler, no report should be generated yet
        reportService.checkAndGenerateReports();
        assertThat(reportRepo.findByDate("2025-10-16")).isEmpty();

        // 3. Travel to 2 AM, the scheduled time
        TimeController.travel(Duration.ofHours(2));

        // 4. Run the scheduler again, the report should now be generated
        reportService.checkAndGenerateReports();
        assertThat(reportRepo.findByDate("2025-10-16")).isNotEmpty();
    }
}
```

## Building From Source

To build the project locally, clone the repository and run the following command:

```bash
mvn clean install
```

## Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue.

## License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.
