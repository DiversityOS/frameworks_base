/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.frameworks.perftests.job;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemClock;
import android.perftests.utils.ManualBenchmarkState;
import android.perftests.utils.PerfManualStatusReporter;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.server.job.JobStore;
import com.android.server.job.JobStore.JobSet;
import com.android.server.job.controllers.JobStatus;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class JobStorePerfTests {
    private static final String SOURCE_PACKAGE = "com.android.frameworks.perftests.job";
    private static final int SOURCE_USER_ID = 0;
    private static final int BASE_CALLING_UID = 10079;
    private static final int MAX_UID_COUNT = 10;

    private static Context sContext;
    private static File sTestDir;
    private static JobStore sJobStore;

    private static List<JobStatus> sFewJobs = new ArrayList<>();
    private static List<JobStatus> sManyJobs = new ArrayList<>();

    @Rule
    public PerfManualStatusReporter mPerfManualStatusReporter = new PerfManualStatusReporter();

    @BeforeClass
    public static void setUpOnce() {
        sContext = InstrumentationRegistry.getTargetContext();
        sTestDir = new File(sContext.getFilesDir(), "JobStorePerfTests");
        sJobStore = JobStore.initAndGetForTesting(sContext, sTestDir);

        for (int i = 0; i < 50; i++) {
            sFewJobs.add(createJobStatus("fewJobs", i, BASE_CALLING_UID + (i % MAX_UID_COUNT)));
        }
        for (int i = 0; i < 500; i++) {
            sManyJobs.add(createJobStatus("manyJobs", i, BASE_CALLING_UID + (i % MAX_UID_COUNT)));
        }
    }

    @AfterClass
    public static void tearDownOnce() {
        sTestDir.deleteOnExit();
    }

    private void runPersistedJobWriting(List<JobStatus> jobList) {
        final ManualBenchmarkState benchmarkState = mPerfManualStatusReporter.getBenchmarkState();

        long elapsedTimeNs = 0;
        while (benchmarkState.keepRunning(elapsedTimeNs)) {
            sJobStore.clearForTesting();
            for (JobStatus job : jobList) {
                sJobStore.addForTesting(job);
            }

            final long startTime = SystemClock.elapsedRealtimeNanos();
            sJobStore.writeStatusToDiskForTesting();
            final long endTime = SystemClock.elapsedRealtimeNanos();
            elapsedTimeNs = endTime - startTime;
        }
    }

    @Test
    public void testPersistedJobWriting_fewJobs() {
        runPersistedJobWriting(sFewJobs);
    }

    @Test
    public void testPersistedJobWriting_manyJobs() {
        runPersistedJobWriting(sManyJobs);
    }

    private void runPersistedJobWriting_delta(List<JobStatus> jobList,
            List<JobStatus> jobAdditions, List<JobStatus> jobRemovals) {
        final ManualBenchmarkState benchmarkState = mPerfManualStatusReporter.getBenchmarkState();

        long elapsedTimeNs = 0;
        while (benchmarkState.keepRunning(elapsedTimeNs)) {
            sJobStore.clearForTesting();
            for (JobStatus job : jobList) {
                sJobStore.addForTesting(job);
            }
            sJobStore.writeStatusToDiskForTesting();

            for (JobStatus job : jobAdditions) {
                sJobStore.addForTesting(job);
            }
            for (JobStatus job : jobRemovals) {
                sJobStore.removeForTesting(job);
            }

            final long startTime = SystemClock.elapsedRealtimeNanos();
            sJobStore.writeStatusToDiskForTesting();
            final long endTime = SystemClock.elapsedRealtimeNanos();
            elapsedTimeNs = endTime - startTime;
        }
    }

    @Test
    public void testPersistedJobWriting_delta_fewJobs() {
        List<JobStatus> additions = new ArrayList<>();
        List<JobStatus> removals = new ArrayList<>();
        final int numModifiedUids = MAX_UID_COUNT / 2;
        for (int i = 0; i < sFewJobs.size() / 3; ++i) {
            JobStatus job = createJobStatus("fewJobs", i, BASE_CALLING_UID + (i % numModifiedUids));
            if (i % 2 == 0) {
                additions.add(job);
            } else {
                removals.add(job);
            }
        }
        runPersistedJobWriting_delta(sFewJobs, additions, removals);
    }

    @Test
    public void testPersistedJobWriting_delta_manyJobs() {
        List<JobStatus> additions = new ArrayList<>();
        List<JobStatus> removals = new ArrayList<>();
        final int numModifiedUids = MAX_UID_COUNT / 2;
        for (int i = 0; i < sManyJobs.size() / 3; ++i) {
            JobStatus job = createJobStatus("fewJobs", i, BASE_CALLING_UID + (i % numModifiedUids));
            if (i % 2 == 0) {
                additions.add(job);
            } else {
                removals.add(job);
            }
        }
        runPersistedJobWriting_delta(sManyJobs, additions, removals);
    }

    private void runPersistedJobReading(List<JobStatus> jobList, boolean rtcIsGood) {
        final ManualBenchmarkState benchmarkState = mPerfManualStatusReporter.getBenchmarkState();

        long elapsedTimeNs = 0;
        while (benchmarkState.keepRunning(elapsedTimeNs)) {
            sJobStore.clearForTesting();
            for (JobStatus job : jobList) {
                sJobStore.addForTesting(job);
            }
            sJobStore.writeStatusToDiskForTesting();

            JobSet jobSet = new JobSet();

            final long startTime = SystemClock.elapsedRealtimeNanos();
            sJobStore.readJobMapFromDisk(jobSet, rtcIsGood);
            final long endTime = SystemClock.elapsedRealtimeNanos();
            elapsedTimeNs = endTime - startTime;
        }
    }

    @Test
    public void testPersistedJobReading_fewJobs_goodRTC() {
        runPersistedJobReading(sFewJobs, true);
    }

    @Test
    public void testPersistedJobReading_fewJobs_badRTC() {
        runPersistedJobReading(sFewJobs, false);
    }

    @Test
    public void testPersistedJobReading_manyJobs_goodRTC() {
        runPersistedJobReading(sManyJobs, true);
    }

    @Test
    public void testPersistedJobReading_manyJobs_badRTC() {
        runPersistedJobReading(sManyJobs, false);
    }

    private static JobStatus createJobStatus(String testTag, int jobId, int callingUid) {
        JobInfo jobInfo = new JobInfo.Builder(jobId,
                new ComponentName(sContext, "JobStorePerfTestJobService"))
                .setPersisted(true)
                .build();
        return JobStatus.createFromJobInfo(
                jobInfo, callingUid, SOURCE_PACKAGE, SOURCE_USER_ID, null, testTag);
    }
}
