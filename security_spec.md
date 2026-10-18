# Security Specification — Body Complete (كامل الأجسام)

## 1. Data Invariants
1. **Strict Multi-User Isolation**: Every document under `/users/{userId}/exercises/{exerciseId}`, `/users/{userId}/workout_logs/{logId}`, and `/users/{userId}/daily_stats/{dateKey}` belongs exclusively to `userId`. Only `request.auth != null && request.auth.uid == userId` may read, list, create, update, or delete records in that subtree.
2. **Identity Integrity**: On creation, `incoming().userId == request.auth.uid && request.auth.uid == userId`. On update, `incoming().userId == existing().userId` and `incoming().createdAt == existing().createdAt`.
3. **Path Variable Guard**: Document IDs (`userId`, `exerciseId`, `logId`, `dateKey`) must match `^[a-zA-Z0-9_\-]+$` and be at most 128 characters.
4. **Schema Completeness & Bounded Strings**: All required keys are enforced via `hasAll` and `hasOnly` on create, and `affectedKeys().hasOnly(...)` + `isValid[Entity](incoming())` on update. Every string has explicit `.size()` bounds and every number has realistic domain bounds.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `unauthDb.doc("users/alice/exercises/ex1").get()` -> Reject (`request.auth != null`).
2. **Cross-User Read (ID Spoofing)**: `bobDb.doc("users/alice/exercises/ex1").get()` -> Reject (`request.auth.uid == userId`).
3. **Cross-User Write**: `bobDb.doc("users/alice/workout_logs/log1").set(...)` -> Reject.
4. **Mismatched Payload userId**: `aliceDb.doc("users/alice/exercises/ex1").set({ userId: "bob", ... })` -> Reject (`incoming().userId == request.auth.uid`).
5. **Shadow Update / Ghost Field**: `aliceDb.doc("users/alice/exercises/ex1").update({ isAdmin: true })` -> Reject (`hasOnly` & `affectedKeys().hasOnly(...)`).
6. **Immutable Field Mutation (`userId`)**: `aliceDb.doc("users/alice/exercises/ex1").update({ userId: "bob" })` -> Reject (`incoming().userId == existing().userId`).
7. **Immutable Field Mutation (`createdAt`)**: `aliceDb.doc("users/alice/exercises/ex1").update({ createdAt: newTimestamp })` -> Reject (`incoming().createdAt == existing().createdAt`).
8. **Future Timestamp Injection**: `createdAt > request.time` -> Reject (`incoming().createdAt <= request.time`).
9. **Invalid Enum (`dayId` / `muscleKey` / `nutritionGoal`)**: `dayId: "invalid_day"` -> Reject.
10. **Out-of-Bounds Number**: `setsCount: 999` or `waterCups: -5` -> Reject.
11. **Oversized String (Denial of Wallet)**: `nameAr` or `userNotes` exceeding max character length -> Reject.
12. **Poisoned Document ID**: Document ID with spaces or special characters outside `^[a-zA-Z0-9_\-]+$` -> Reject (`isValidId`).
