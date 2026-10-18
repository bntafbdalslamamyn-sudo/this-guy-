const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validExercisePayload(userId) {
  const ts = new Date(Date.now() - 2000);
  return {
    userId,
    dayId: "sat",
    dayOrder: 1,
    nameAr: "ضغط بار مستوي (Barbell Bench Press)",
    nameEn: "Barbell Bench Press",
    muscleGroupAr: "الصدر",
    muscleGroupEn: "Chest",
    muscleKey: "chest",
    setsCount: 4,
    repsRange: "8 - 12 تكرار",
    restSeconds: 90,
    instructionsAr: "استلقِ على البنش المستوي وامسك البار بقبضة أوسع قليلًا من الكتفين.",
    instructionsEn: "Lie flat on the bench and grip the barbell slightly wider than shoulder-width.",
    notesAr: "حافظ على ثبات لوحي الكتف على البنش.",
    lastWeightKg: 60,
    isCustomModified: false,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validWorkoutLogPayload(userId) {
  const ts = new Date(Date.now() - 2000);
  return {
    userId,
    dayId: "sat",
    dayTitleAr: "السبت — صدر وتراي",
    exerciseId: "sat_chest_1",
    exerciseNameAr: "ضغط بار مستوي (Barbell Bench Press)",
    muscleKey: "chest",
    completedSets: 4,
    targetSets: 4,
    weightKg: 65,
    repsCompleted: "12, 10, 10, 8",
    durationMinutes: 45,
    userNotes: "تمرين قوي وأداء ممتاز",
    dateKey: "2026-10-17",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validDailyStatsPayload(userId) {
  const ts = new Date(Date.now() - 2000);
  return {
    userId,
    dateKey: "2026-10-17",
    waterCups: 5,
    waterTargetCups: 8,
    bodyWeightKg: 78.5,
    stepsCount: 8420,
    sleepHours: 7.5,
    chestCm: 106,
    armCm: 40,
    waistCm: 82,
    thighCm: 60,
    nutritionGoal: "bulk",
    targetCalories: 2650,
    consumedCalories: 1850,
    consumedProteinG: 145,
    consumedCarbsG: 210,
    consumedFatG: 55,
    mealsLogSummary: "وجبة الإفطار: شوفان وبيض (520 سعرة) | الغداء: صدور دجاج وأرز (780 سعرة)",
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or create exercises", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("exercises").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("exercises").doc("ex_1").set(validExercisePayload(ALICE_UID))
  );
});

test("Authenticated owner can create, read, list, and update their own exercise", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("exercises").doc("ex_1");
  await assertSucceeds(docRef.set(validExercisePayload(ALICE_UID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("exercises").where("userId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    docRef.update({
      lastWeightKg: 70,
      isCustomModified: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Cross-user access is rejected for exercises, workout_logs, and daily_stats", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("exercises").doc("ex_1").set(validExercisePayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("workout_logs").doc("log_1").set(validWorkoutLogPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("daily_stats").doc("2026-10-17").set(validDailyStatsPayload(ALICE_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("exercises").doc("ex_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("workout_logs").doc("log_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("daily_stats").doc("2026-10-17").get());
});

test("Shadow update with ghost field or invalid enum is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("exercises").doc("ex_1");
  await assertSucceeds(docRef.set(validExercisePayload(ALICE_UID)));

  await assertFails(
    docRef.update({
      isVerified: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertFails(
    docRef.update({
      dayId: "invalid_day",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
