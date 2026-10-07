package com.graduation.topic.service;

/** Chạy bằng Java, không cần MySQL/Maven/Spring; dùng chính policy của ứng dụng. */
public final class TopicCapacityPolicyChecks {

    private static int passed;

    public static void main(String[] args) {
        equal("approve first", 1, calculate(0, 1, true, null, true, 1));
        equal("pending does not occupy", 0, calculate(0, 1, true, null, false, 0));
        equal("repeat approval", 1, calculate(1, 1, true, state(true, 1), true, 1));
        equal("new version same allocation", 1,
                calculate(1, 1, true, state(true, 1), true, 2));
        equal("reject approved", 0, calculate(1, 1, true, state(true, 1), false, 2));
        equal("repeat rejection", 0, calculate(0, 1, true, state(false, 2), false, 2));
        equal("approve after rejection", 1,
                calculate(0, 1, true, state(false, 2), true, 3));
        equal("release when topic closed", 0,
                calculate(1, 1, false, state(true, 1), false, 2));
        equal("replay approval when closed", 1,
                calculate(1, 1, false, state(true, 1), true, 1));
        rejects("full topic", () -> calculate(1, 1, true, null, true, 1));
        rejects("closed topic", () -> calculate(0, 1, false, null, true, 1));
        rejects("late approval after release",
                () -> calculate(0, 1, true, state(false, 2), true, 1));
        rejects("same version conflicting target",
                () -> calculate(1, 1, true, state(true, 1), false, 1));
        rejects("do not become negative",
                () -> calculate(0, 1, true, state(true, 1), false, 2));
        rejects("invalid maximum", () -> calculate(0, 0, true, null, true, 1));
        rejects("invalid existing count", () -> calculate(2, 1, true, null, false, 1));
        rejects("negative version", () -> calculate(0, 1, true, null, true, -1));
        rejects("late release after reapproval",
                () -> calculate(1, 1, true, state(true, 3), false, 2));
        System.out.println("PASS: " + passed + " capacity policy checks");
    }

    private static TopicCapacityPolicy.PreviousState state(boolean approved, long version) {
        return new TopicCapacityPolicy.PreviousState(approved, version);
    }

    private static int calculate(int count, int max, boolean open,
            TopicCapacityPolicy.PreviousState previous, boolean approved, long version) {
        return TopicCapacityPolicy.calculate(count, max, open, previous, approved, version);
    }

    private static void equal(String name, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
        passed++;
    }

    private static void rejects(String name, Runnable action) {
        try {
            action.run();
        } catch (IllegalStateException | IllegalArgumentException expected) {
            passed++;
            return;
        }
        throw new AssertionError(name + ": expected rejection");
    }
}
