# Project Overview

OSGi bundle that provides a `SlingRepository` implementation backed by Apache Jackrabbit Oak. It wires Oak's `NodeStore`, security, indexing, and whiteboard services into the Sling JCR layer, and exposes the result as a JCR `Repository` service in the OSGi registry. The bundle also ships a Lucene index initializer and a thread pool registrar. There is no standalone server — the bundle runs inside a Sling/Felix OSGi container.

# Core Commands

```bash
# Build and package (skips integration tests)
mvn clean package -DskipTests

# Full build including integration tests
mvn clean verify

# Run only integration tests (bundle must already be built)
mvn failsafe:integration-test failsafe:verify

# Run a single integration test class
mvn verify -Dit.test=OakServerIT

# Check OSGi baseline (API compatibility)
mvn verify -Dbaseline.skip=false

# Apply code formatting (Spotless)
mvn spotless:apply

# Check formatting without applying
mvn spotless:check

# Generate sources/javadoc
mvn javadoc:javadoc
```

No dev server — this is a library bundle, not a standalone application.

# Project Layout

```
pom.xml                         Maven build descriptor
bnd.bnd                         OSGi bundle manifest overrides (Import-Package, Provide-Capability)
src/
  main/java/org/apache/sling/jcr/oak/server/internal/
    OakSlingRepositoryManager.java        OSGi component; bootstraps Oak and registers the JCR Repository service
    OakSlingRepository.java               SlingRepository implementation wrapping Oak
    OakSlingRepositoryManagerConfiguration.java  @ObjectClassDefinition for manager config
    DefaultThreadPoolRegistrar.java       Registers a Sling ThreadPool / Executor as an OSGi service
    TcclWrappingJackrabbitRepository.java TCCL-aware Repository delegate
    TcclWrappingJackrabbitSession.java    TCCL-aware Session delegate
    package-info.java                     Internal package docs; package is intentionally not exported
    index/
      LuceneIndexRepositoryInitializer.java        Registers Lucene full-text index definitions
      LuceneIndexRepositoryInitializerConfiguration.java  Config for the Lucene initializer
  test/java/org/apache/sling/jcr/oak/server/it/
    OakServerTestSupport.java     Base class: PaxExam config, bundle provisioning
    OakServerIT.java              Core repository integration tests
    LoginAdminBlacklistedIT.java  loginAdministrative() blacklist tests
    LoginAdminWhitelistedIT.java  loginAdministrative() whitelist tests
    LuceneIndexIT.java            Lucene index integration tests
    ResourceTypeResolutionIT.java Sling resource-type resolution tests
    Sling9719IT.java / Sling9826IT.java  Regression tests for SLING issues
    ResourceEventListener.java    OSGi EventAdmin listener helper used by integration tests
    Retry.java                    Retry helper utility for timing-sensitive integration assertions
  test/resources/
    repoinit.txt                  Repository initialisation script used in tests
    i18n.cnd                      CND node type definition for i18n tests
target/                           Build output (not committed)
```

# Development Patterns & Constraints

- **Java 8** source/target (`sling.java.version=8`); do not use Java 9+ APIs in `src/main`.
- **OSGi R6/R7 annotations** (`org.osgi.service.component.annotations`). Do not use legacy Felix SCR annotations.
- All OSGi components use `@Component`, `@Activate`, `@Deactivate`, `@Reference` from `org.osgi.service.component.annotations`.
- Metatype configuration via `@ObjectClassDefinition` / `@AttributeDefinition` from `org.osgi.service.metatype.annotations`.
- Internal implementation classes live in the `*.internal` package, which is not exported. Keep the public API surface at zero — there are no exported packages beyond what bnd generates.
- `bnd.bnd` controls `Import-Package` ranges. When updating Oak/Jackrabbit dependencies, verify import version ranges in `bnd.bnd` are still valid.
- Current dependency baselines in this branch: Jackrabbit `2.16.3`, Oak `1.56.0`, Pax Exam `4.13.3`.
- **Spotless** enforces formatting. Run `mvn spotless:apply` before committing.
- ASF license header required on every source file.
- 4-space indentation, no tabs. Follow existing code style.

# Git Workflow

- Branching: feature branches off `master`; name them descriptively (e.g., `feature/SLING-XXXX-description`).
- Commit messages: start with the JIRA issue key when applicable — `SLING-XXXX Short description`.
- No direct pushes to `master`; raise a GitHub PR or use the Apache GitBox mirror.
- See [Apache Sling contribution guide](https://sling.apache.org/contributing.html) for full process.

# Testing Guidelines

- **Framework**: JUnit 4 + PaxExam 4 (`pax-exam-junit4`) for integration tests; no unit tests exist in this bundle.
- Integration test classes use the `*IT` naming convention and are picked up by `maven-failsafe-plugin`; helper test utilities may not use the `IT` suffix.
- Tests run inside a forked OSGi container provisioned by `OakServerTestSupport` (Felix framework + Sling bundles).
- Place new integration tests in `src/test/java/org/apache/sling/jcr/oak/server/it/` extending `OakServerTestSupport`.
- Test output is redirected to files under `target/failsafe-reports/` (`redirectTestOutputToFile=true`); check `*-output.txt` for console logs on failure.
- No coverage tooling is configured — adding JaCoCo requires a POM change.

# Gotchas

- Integration tests fork a full OSGi container (PaxExam forked container). They are slow and require the bundle JAR in `target/` — always run `mvn package` before running tests in isolation.
- The `bundle.filename` system property must point to the built JAR; it is set automatically by the Failsafe plugin but must be set manually if you invoke tests outside Maven.
- `--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED` JVM flags are required for PaxExam on Java 11+; they are configured in the Failsafe `argLine` but must be added manually for IDE test runs.
- The Lucene index initializer is optional (Oak Lucene dependency is `resolution:=optional`). If `oak-lucene` is absent, `LuceneIndexRepositoryInitializer` deactivates gracefully.
- MongoDB support is wired as `resolution:=optional` in `bnd.bnd`. Do not add hard runtime dependencies on `com.mongodb`.
- `TcclWrappingJackrabbitRepository` and `TcclWrappingJackrabbitSession` exist solely to set the thread context classloader for Oak code that relies on TCCL-based service loading. Do not bypass these wrappers.
- OSGi baseline checks compare against the last released version. A `@ConsumerType`/`@ProviderType` mismatch or accidental package export will fail the baseline check.

# Security

<!-- sling-security-default:start -->
The threat model for this project is https://github.com/apache/sling/blob/master/docs/threat-model.md .
<!-- sling-security-default:end -->

