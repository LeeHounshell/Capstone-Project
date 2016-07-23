package com.harlie.radiotheater.radiomysterytheater;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@Suite.SuiteClasses({ ExampleInstrumentationTest.class })
public class ExampleInstrumentationTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        //#IFDEF 'PAID'
        //assertEquals("com.harlie.radiotheater.radiomysterytheater.paid", appContext.getPackageName());
        //#ENDIF

        //#IFDEF 'TRIAL'
        assertEquals("com.harlie.radiotheater.radiomysterytheater", appContext.getPackageName());
        //#ENDIF

    }
}
