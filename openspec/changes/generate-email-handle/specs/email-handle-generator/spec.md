## ADDED Requirements

### Requirement: Derive base trigram from mailbox segments
The service SHALL inspect the email mailbox (portion before `@`) and, when a dot `.` exists, assemble the proposed trigram using the character immediately before the first dot followed by the next two characters after that dot. If fewer than three usable characters exist around the dot, the system SHALL continue scanning the mailbox to gather enough alphabetical characters. If no dot exists, the service SHALL instead take the first three alphabetical characters of the mailbox, preserving order.

#### Scenario: Dot-separated mailbox yields trigram
- **WHEN** the email is `anna.larsson@example.se`
- **THEN** the derived trigram SHALL be `ALA` (character `a` before the dot plus `la` after).

#### Scenario: Mailbox without dot falls back to first letters
- **WHEN** the email is `emil@example.se`
- **THEN** the derived trigram SHALL be `EMI` (the first three alphabetical characters in order).

### Requirement: Preserve casing and sanitize source characters
The system SHALL uppercase all candidate characters to enforce consistent handles. Non-letter characters SHALL be skipped when forming the trigram so digits, emojis, or punctuation never appear in the final handle. When insufficient alphabetical characters exist, the service SHALL pad using `X` placeholders to reach three letters.

#### Scenario: Non-letter is skipped and handle uppercased
- **WHEN** the mailbox is `bo.b-å@example.se`
- **THEN** the resulting handle SHALL be `OBÅ` (letters only, uppercase) with no punctuation.

#### Scenario: Padding occurs when too short
- **WHEN** the mailbox is `q@example.se`
- **THEN** the resulting handle SHALL be `QXX`.

### Requirement: Reject handles that match the Swedish profanity blocklist
The service SHALL maintain a configurable list of forbidden three-letter combinations (seed list contains `KUK`, `FAN`, `FIT`, `PIS`, etc.). Whenever a derived trigram matches any forbidden entry, the system SHALL attempt the next available trigram candidate by shifting one character to the right in the mailbox, repeating until an allowed trigram is found or all possibilities are exhausted. If no non-blocked trigram exists, the request SHALL fail with a descriptive error payload.

#### Scenario: Blocklist hit triggers alternate trigram
- **WHEN** the derived trigram would be `KUK`
- **THEN** the service SHALL attempt a different three-letter window and only return once a non-blocked trigram is found.

#### Scenario: No safe trigram produces failure
- **WHEN** every possible trigram is blocked
- **THEN** the service SHALL respond with HTTP 422 and an error indicating that no acceptable handle can be generated.

### Requirement: Guarantee uniqueness by reserving handles
Before returning a handle, the system SHALL check a persistent reservation store to confirm the trigram is unused. If the trigram exists, the service SHALL attempt an alternative trigram by sliding to the next feasible character window. When all trigram windows are either blocked or already reserved, the service SHALL append a numeric tie-breaker (1-9) to the trigram and retry the uniqueness check up to 9 attempts. If still colliding, the request SHALL fail with HTTP 409 "exhausted".

#### Scenario: Collision triggers next window
- **WHEN** the preferred trigram is already reserved
- **THEN** the service SHALL try the next trigram window before attempting numeric suffixes.

#### Scenario: Numeric suffix ensures uniqueness
- **WHEN** all trigram windows collide but `BOB1` is unused
- **THEN** the service SHALL return `BOB1` and persist the reservation.

### Requirement: Provide REST interface for handle allocation
The service SHALL expose `POST /usernames` accepting JSON `{ "email": "<address>" }`. On success it SHALL return HTTP 201 with body `{ "handle": "<value>", "reserved": true }`. Invalid emails SHALL produce HTTP 400 validation errors; failure to find an acceptable handle SHALL return HTTP 422 as described above; persistence failures SHALL return HTTP 503.

#### Scenario: Successful request returns handle payload
- **WHEN** a valid email that yields an available handle is submitted
- **THEN** the API SHALL respond with HTTP 201 and a JSON body containing the generated handle and reservation confirmation.

#### Scenario: Invalid email rejected
- **WHEN** the email string omits an `@`
- **THEN** the API SHALL respond with HTTP 400 and a validation error message.
