# ADR 0001: Sample-first shared app

Status: accepted · 7 October 2026

## Context

We have a four-floor navigation concept but no surveyed plans, positioning hardware, deployment or accounts. Android and iOS should share behavior and UI. Open-source contributors need a runnable starting point.

## Decision

Use Kotlin Multiplatform with Compose Multiplatform, a shared feature-based module, thin platform hosts, and a desktop preview. Use fictional meter-based building geometry. Keep domain behavior pure and test it in matching feature packages. Provide real graph routing while clearly labelling checkpoint, sharing and team simulations. Store demo state only in memory.

Use modern Android KMP library tooling with a separate Android app module. Pin dependencies and check in the wrapper and Xcode project. Do not split every feature into a Gradle module until dependency enforcement or build performance justifies the cost.

## Consequences

One implementation drives mobile and desktop behavior. Tests can run on the JVM without a phone. Domain packages make future map/position adapters feasible without inventing them now. Sample state and platform builds do not prove suitability for a real building; production gates remain explicit in the PRD.
