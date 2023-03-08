package com.epam.deltix.gflog.benchmark.util;

class BenchmarkStatePadding {

    byte p000, p001, p002, p003, p004, p005, p006, p007, p008, p009, p010, p011, p012, p013, p014, p015;
    byte p016, p017, p018, p019, p020, p021, p022, p023, p024, p025, p026, p027, p028, p029, p030, p031;
    byte p032, p033, p034, p035, p036, p037, p038, p039, p040, p041, p042, p043, p044, p045, p046, p047;
    byte p048, p049, p050, p051, p052, p053, p054, p055, p056, p057, p058, p059, p060, p061, p062, p063;
    byte p064, p065, p066, p067, p068, p069, p070, p071, p072, p073, p074, p075, p076, p077, p078, p079;
    byte p080, p081, p082, p083, p084, p085, p086, p087, p088, p089, p090, p091, p092, p093, p094, p095;
    byte p096, p097, p098, p099, p100, p101, p102, p103, p104, p105, p106, p107, p108, p109, p110, p111;
    byte p112, p113, p114, p115, p116, p117, p118, p119, p120, p121, p122, p123, p124, p125, p126, p127;
    byte p128, p129, p130, p131, p132, p133, p134, p135, p136, p137, p138, p139, p140, p141, p142, p143;
    byte p144, p145, p146, p147, p148, p149, p150, p151, p152, p153, p154, p155, p156, p157, p158, p159;
    byte p160, p161, p162, p163, p164, p165, p166, p167, p168, p169, p170, p171, p172, p173, p174, p175;
    byte p176, p177, p178, p179, p180, p181, p182, p183, p184, p185, p186, p187, p188, p189, p190, p191;
    byte p192, p193, p194, p195, p196, p197, p198, p199, p200, p201, p202, p203, p204, p205, p206, p207;
    byte p208, p209, p210, p211, p212, p213, p214, p215, p216, p217, p218, p219, p220, p221, p222, p223;
    byte p224, p225, p226, p227, p228, p229, p230, p231, p232, p233, p234, p235, p236, p237, p238, p239;
    byte p240, p241, p242, p243, p244, p245, p246, p247, p248, p249, p250, p251, p252, p253, p254, p255;

}

class BenchmarkStateFields extends BenchmarkStatePadding {

    public String arg0 = "string";
    public char arg1 = 'c';
    public int arg2 = 1234567;
    public long arg3 = 12345678901234L;
    public String arg4 = "string";
    public String arg5 = "string";
    public char arg6 = 'c';
    public int arg7 = 1234567;
    public long arg8 = 12345678901234L;
    public String arg9 = "string";
    public Throwable exception = BenchmarkUtil.EXCEPTION;
    private int depth = -1;

    public Object newObject() {
        if (depth < 0) {
            depth = findDepth();
        }

        return newObject(depth);
    }

    public Throwable newException() {
        if (depth < 0) {
            depth = findDepth();
        }

        return newException(depth);
    }

    public Throwable newExceptionWithoutStack() {
        if (depth < 0) {
            depth = findDepth();
        }

        return newExceptionWithoutStack(depth);
    }

    private static int findDepth() {
        final Thread thread = Thread.currentThread();
        final StackTraceElement[] stack = thread.getStackTrace();

        final int depth = BenchmarkUtil.EXCEPTION_STACK_DEPTH - stack.length + 1;

        if (depth < 0) {
            throw new IllegalArgumentException("Current thread stack depth is more than requested");
        }

        return depth;
    }

    private static Object newObject(final int depth) {
        if (depth <= 0) {
            return new Object();
        }

        return newObject(depth - 1);
    }

    private static Throwable newException(final int depth) {
        if (depth <= 0) {
            return new Throwable("my-exception-with-stack");
        }

        return newException(depth - 1);
    }

    private static Throwable newExceptionWithoutStack(final int depth) {
        if (depth <= 0) {
            return new Throwable("my-exception-without-stack") {

                @Override
                public Throwable fillInStackTrace() { // lgtm [java/non-sync-override]
                    return this;                      // false positive
                }

            };
        }

        return newExceptionWithoutStack(depth - 1);
    }

}

public class BenchmarkState extends BenchmarkStateFields {

    byte p256, p257, p258, p259, p260, p261, p262, p263, p264, p265, p266, p267, p268, p269, p270, p271;
    byte p272, p273, p274, p275, p276, p277, p278, p279, p280, p281, p282, p283, p284, p285, p286, p287;
    byte p288, p289, p290, p291, p292, p293, p294, p295, p296, p297, p298, p299, p300, p301, p302, p303;
    byte p304, p305, p306, p307, p308, p309, p310, p311, p312, p313, p314, p315, p316, p317, p318, p319;
    byte p320, p321, p322, p323, p324, p325, p326, p327, p328, p329, p330, p331, p332, p333, p334, p335;
    byte p336, p337, p338, p339, p340, p341, p342, p343, p344, p345, p346, p347, p348, p349, p350, p351;
    byte p352, p353, p354, p355, p356, p357, p358, p359, p360, p361, p362, p363, p364, p365, p366, p367;
    byte p368, p369, p370, p371, p372, p373, p374, p375, p376, p377, p378, p379, p380, p381, p382, p383;
    byte p384, p385, p386, p387, p388, p389, p390, p391, p392, p393, p394, p395, p396, p397, p398, p399;
    byte p400, p401, p402, p403, p404, p405, p406, p407, p408, p409, p410, p411, p412, p413, p414, p415;
    byte p416, p417, p418, p419, p420, p421, p422, p423, p424, p425, p426, p427, p428, p429, p430, p431;
    byte p432, p433, p434, p435, p436, p437, p438, p439, p440, p441, p442, p443, p444, p445, p446, p447;
    byte p448, p449, p450, p451, p452, p453, p454, p455, p456, p457, p458, p459, p460, p461, p462, p463;
    byte p464, p465, p466, p467, p468, p469, p470, p471, p472, p473, p474, p475, p476, p477, p478, p479;
    byte p480, p481, p482, p483, p484, p485, p486, p487, p488, p489, p490, p491, p492, p493, p494, p495;
    byte p496, p497, p498, p499, p500, p501, p502, p503, p504, p505, p506, p507, p508, p509, p510, p511;

}
