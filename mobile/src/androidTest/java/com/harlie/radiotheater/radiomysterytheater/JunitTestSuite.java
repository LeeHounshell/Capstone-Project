package com.harlie.radiotheater.radiomysterytheater;

import com.harlie.radiotheater.radiomysterytheater.data.TestRadioTheaterContract;
import com.harlie.radiotheater.radiomysterytheater.data.TestRadioTheaterDb;
import com.harlie.radiotheater.radiomysterytheater.data.TestRadioTheaterUtilities;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ExampleInstrumentationTest.class,
        TestRadioTheaterContract.class,
        TestRadioTheaterUtilities.class,
        TestRadioTheaterDb.class
})
public class JunitTestSuite {
}
