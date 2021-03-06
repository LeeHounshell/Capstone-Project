/*
 * Copyright (C) 2014 The Android Open Source Project
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

//
// THIS CODE IS REPURPOSED FROM THE GOOGLE UNIVERSAL-MEDIA-PLAYER SAMPLE
//

package com.harlie.radiotheater.radiomysterytheater.utils;

import android.util.Log;

public class LogHelper {

    private static final String LOG_PREFIX = "harlie_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }
        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void v(String tag, Object... messages) {
        // Only log VERBOSE if build type is DEBUG
        //if (BuildConfig.DEBUG) {
            log(tag, Log.VERBOSE, null, messages);
        //}
    }

    public static void d(String tag, Object... messages) {
        // Only log DEBUG if build type is DEBUG
        //if (BuildConfig.DEBUG) {
            log(tag, Log.DEBUG, null, messages);
        //}
    }

    public static void i(String tag, Object... messages) {
        log(tag, Log.INFO, null, messages);
    }

    public static void w(@SuppressWarnings("SameParameterValue") String tag, Object... messages) {
        log(tag, Log.WARN, null, messages);
    }

    public static void w(@SuppressWarnings("SameParameterValue") String tag, Throwable t, Object... messages) {
        log(tag, Log.WARN, t, messages);
    }

    public static void e(@SuppressWarnings("SameParameterValue") String tag, Object... messages) {
        log(tag, Log.ERROR, null, messages);
    }

    public static void e(String tag, Throwable t, Object... messages) {
        log(tag, Log.ERROR, t, messages);
    }

    public static void log(String tag, int level, Throwable t, Object... messages) {
        //tag = tag.substring(0, Math.min(tag.length(), MAX_LOG_TAG_LENGTH));
        // FIXME
        //if (Log.isLoggable(tag, level)) {
            String message;
            if (t == null && messages != null && messages.length == 1) {
                // handle this common case without the extra cost of creating a stringbuffer:
                //message = messages[0].toString().replace("com.harlie.radiotheater.radiomysterytheater", "harlie.");
                message = messages[0].toString();
            } else {
                StringBuilder sb = new StringBuilder();
                if (messages != null) for (Object m : messages) {
                    sb.append(m);
                }
                if (t != null) {
                    sb.append("\n").append(Log.getStackTraceString(t));
                }
                //message = sb.toString().replace("com.harlie.radiotheater.radiomysterytheater", "harlie.");
                message = sb.toString();
            }
            //Log.println(level, tag, message);
            switch (level) {
                case Log.ERROR: {
                    Log.e(tag, message);
                    break;
                }
                case Log.WARN: {
                    Log.w(tag, message);
                    break;
                }
                case Log.INFO: {
                    Log.i(tag, message);
                    break;
                }
                case Log.DEBUG: {
                    Log.d(tag, message);
                    break;
                }
                case Log.VERBOSE: {
                    Log.v(tag, message);
                    break;
                }
            }
        //}
    }
}
