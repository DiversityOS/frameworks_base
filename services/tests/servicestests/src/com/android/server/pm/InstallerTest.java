/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.SystemClock;
import android.os.UserHandle;
import android.test.AndroidTestCase;
import android.util.Log;

import com.android.internal.util.ArrayUtils;

import java.util.Arrays;

public class InstallerTest extends AndroidTestCase {
    private static final String TAG = "InstallerTest";

    private Installer mInstaller;

    private final Timer mManual = new Timer("Manual");
    private final Timer mQuota = new Timer("Quota");

    private static class Timer {
        private final String mTitle;
        private long mStart;
        private long mTotal;

        public Timer(String title) {
            mTitle = title;
        }

        public void start() {
            mStart = SystemClock.currentTimeMicro();
        }

        public void stop() {
            mTotal += SystemClock.currentTimeMicro() - mStart;
        }

        public void reset() {
            mStart = 0;
            mTotal = 0;
        }

        @Override
        public String toString() {
            return mTitle + ": " + (mTotal / 1000) + "ms";
        }
    }

    @Override
    public void setUp() throws Exception {
        mInstaller = new Installer(getContext());
        mInstaller.onStart();
        mManual.reset();
        mQuota.reset();
    }

    @Override
    public void tearDown() throws Exception {
        Log.i(TAG, mManual.toString());
        Log.i(TAG, mQuota.toString());
        mInstaller = null;
    }

    public void testGetAppSize() throws Exception {
        int[] appIds = null;

        final PackageManager pm = getContext().getPackageManager();
        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
            final int userId = UserHandle.getUserId(app.uid);
            final int appId = UserHandle.getAppId(app.uid);

            if (ArrayUtils.contains(appIds, appId)) {
                continue;
            } else {
                appIds = ArrayUtils.appendInt(appIds, appId);
            }

            final String[] packageNames = pm.getPackagesForUid(app.uid);
            final long[] ceDataInodes = new long[packageNames.length];
            final String[] codePaths = new String[packageNames.length];

            for (int i = 0; i < packageNames.length; i++) {
                final ApplicationInfo info = pm.getApplicationInfo(packageNames[i], 0);
                codePaths[i] = info.getCodePath();
            }

            final PackageStats stats = new PackageStats(app.packageName);
            final PackageStats quotaStats = new PackageStats(app.packageName);

            mManual.start();
            mInstaller.getAppSize(app.volumeUuid, packageNames, userId, 0,
                    appId, ceDataInodes, codePaths, stats);
            mManual.stop();

            mQuota.start();
            mInstaller.getAppSize(app.volumeUuid, packageNames, userId, Installer.FLAG_USE_QUOTA,
                    appId, ceDataInodes, codePaths, quotaStats);
            mQuota.stop();

            checkEquals(Arrays.toString(packageNames) + " UID=" + app.uid, stats, quotaStats);
        }
    }

    public void testGetUserSize() throws Exception {
        int[] appIds = null;

        final PackageManager pm = getContext().getPackageManager();
        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
            final int appId = UserHandle.getAppId(app.uid);
            if (!ArrayUtils.contains(appIds, appId)) {
                appIds = ArrayUtils.appendInt(appIds, appId);
            }
        }

        final PackageStats stats = new PackageStats("android");
        final PackageStats quotaStats = new PackageStats("android");

        mManual.start();
        mInstaller.getUserSize(null, UserHandle.USER_SYSTEM, 0,
                appIds, stats);
        mManual.stop();

        mQuota.start();
        mInstaller.getUserSize(null, UserHandle.USER_SYSTEM, Installer.FLAG_USE_QUOTA,
                appIds, quotaStats);
        mQuota.stop();

        checkEquals(Arrays.toString(appIds), stats, quotaStats);
    }

    public void testGetExternalSize() throws Exception {
        mManual.start();
        final long[] stats = mInstaller.getExternalSize(null, UserHandle.USER_SYSTEM, 0);
        mManual.stop();

        mQuota.start();
        final long[] quotaStats = mInstaller.getExternalSize(null, UserHandle.USER_SYSTEM,
                Installer.FLAG_USE_QUOTA);
        mQuota.stop();

        for (int i = 0; i < stats.length; i++) {
            checkEquals("#" + i, stats[i], quotaStats[i]);
        }
    }

    private static void checkEquals(String msg, PackageStats a, PackageStats b) {
        checkEquals(msg + " codeSize", a.codeSize, b.codeSize);
        checkEquals(msg + " dataSize", a.dataSize, b.dataSize);
        checkEquals(msg + " cacheSize", a.cacheSize, b.cacheSize);
        checkEquals(msg + " externalCodeSize", a.externalCodeSize, b.externalCodeSize);
        checkEquals(msg + " externalDataSize", a.externalDataSize, b.externalDataSize);
        checkEquals(msg + " externalCacheSize", a.externalCacheSize, b.externalCacheSize);
    }

    private static void checkEquals(String msg, long expected, long actual) {
        if (expected != actual) {
            Log.e(TAG, msg + " expected " + expected + " actual " + actual);
        }
    }
}
