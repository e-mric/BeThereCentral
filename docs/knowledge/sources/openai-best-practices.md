# OpenAI best-practices reference

Source ID: `openai-codex-best-practices`. Read 16 October 2026: [official guide](https://learn.chatgpt.com/guides/best-practices). Machine-readable provenance is in `../sources.json`.

The guide recommends clear task goals, context, constraints and completion criteria; planning complex work; concise persistent project instructions; selective use of connected tools; reusable skills for repeated workflows; and bounded parallel work. Scheduled work should build on a dependable workflow.

## Application here

The repository already has model roles, privacy boundaries and feature-specific testing rules. [WORKFLOW](../../agents/WORKFLOW.md) and [PLANS](../../../PLANS.md) make task context, verification and handoff explicit. The [knowledge maintenance procedure](../README.md) supports source retrieval without duplicating canonical requirements. A local skill and structural checker make that procedure repeatable.

These are project choices informed by the guide, not additional product capabilities. No account settings, connectors, models or automations are enabled or changed. This source is general guidance, not evidence that an app feature works.
