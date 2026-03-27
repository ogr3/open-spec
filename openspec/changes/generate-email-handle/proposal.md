## Why

A short onboarding flow needs an automatic way to mint memorable usernames from an email address. Today reviewers must manually craft handles and double-check for offensive Swedish trigrams, slowing signups. Automating this step ensures consistent, culturally aware handles and eliminates manual moderation.

## What Changes

- Build a REST endpoint that accepts an email address and returns a unique three-character username derived from the mailbox name.
- Persist and consult a Swedish profanity blocklist so disallowed trigrams are never issued.
- Provide collision management when a derived trigram is already taken by trying alternative character slices and appending tie-break suffixes.
- Record allocations in lightweight storage so uniqueness is enforced across requests and environments.

## Capabilities

### New Capabilities
- `email-handle-generator`: Defines the rules for deriving three-character usernames from emails, managing retries, handling collisions, consulting the blocklist, and exposing the REST contract for requesting handles.

### Modified Capabilities
- _None_

## Impact

- Introduces a Spring Boot-based REST surface (`POST /usernames`) deployable alongside onboarding services.
- Requires storage for issued handles and a configurable profanity blocklist source.
- Adds validation utilities, integration tests that cover derivation edge cases, and CI wiring for new packages/modules.
